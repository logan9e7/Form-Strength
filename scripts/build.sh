#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${ANDROID_PLATFORM_JAR:?Set ANDROID_PLATFORM_JAR to android.jar}"
: "${ANDROID_BUILD_TOOLS:?Set ANDROID_BUILD_TOOLS}"
: "${ECJ_JAR:?Set ECJ_JAR}"

# Read release metadata once. Never reuse a prior artifact after a failed build.
readarray -t manifest_values < <(python3 - <<'PY'
import re, xml.etree.ElementTree as ET
root=ET.parse('app/src/main/AndroidManifest.xml').getroot()
a='{http://schemas.android.com/apk/res/android}'
version=root.get(a+'versionName','')
if not re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9._-]{0,63}',version):
 raise SystemExit('Invalid Android versionName for release filename')
code=root.get(a+'versionCode','')
if not re.fullmatch(r'[1-9][0-9]{0,9}',code) or int(code)>2100000000:
 raise SystemExit('Invalid Android versionCode for release')
print(version)
print(code)
print(root.find('uses-sdk').get(a+'minSdkVersion'))
PY
)
[[ ${#manifest_values[@]} == 3 ]] || { echo 'Could not read release metadata.' >&2; exit 1; }
# Version metadata is owned by the manifest. Optional CI guards reject mismatched
# release inputs instead of producing a filename that misrepresents the APK.
[[ -z "${APK_VERSION_NAME:-}" || "$APK_VERSION_NAME" == "${manifest_values[0]}" ]] || { echo 'APK_VERSION_NAME does not match the manifest.' >&2; exit 1; }
[[ -z "${APK_VERSION_CODE:-}" || "$APK_VERSION_CODE" == "${manifest_values[1]}" ]] || { echo 'APK_VERSION_CODE does not match the manifest.' >&2; exit 1; }
release_name="Form-Strength-${manifest_values[0]}"
min_api="${manifest_values[2]}"
[[ "$min_api" =~ ^[0-9]+$ ]] || { echo 'Invalid minimum API.' >&2; exit 1; }
if [[ -n "${APK_KEYSTORE:-}" ]]; then
 : "${APK_KEYSTORE_PASSWORD:?Set signing password in environment}"
 release_path="outputs/$release_name.apk"
else
 release_path="outputs/$release_name-unsigned.apk"
fi
if [[ -n "${APK_OUTPUT_NAME:-}" ]]; then
 [[ "$APK_OUTPUT_NAME" =~ ^[A-Za-z0-9][A-Za-z0-9._-]{0,120}\.apk$ ]] || { echo 'APK_OUTPUT_NAME must be an APK basename without directories.' >&2; exit 1; }
 release_path="outputs/$APK_OUTPUT_NAME"
fi
export release_path
python3 - <<'PY'
import os, shutil
from pathlib import Path
# All paths are fixed, repository-relative build outputs. Keep other releases/tests.
for name in ('classes','dex','compiled'):
 p=Path('build')/name
 if p.is_symlink() or p.is_file():p.unlink()
 elif p.exists():shutil.rmtree(p)
 p.mkdir(parents=True)
for name in ('base.apk','aligned.apk','classes.jar','sources.args','release.apk'):
 (Path('build')/name).unlink(missing_ok=True)
Path('outputs').mkdir(exist_ok=True)
Path(os.environ['release_path']).unlink(missing_ok=True)
sources=sorted(Path('app/src/main/java').rglob('*.java'))
if not sources:raise SystemExit('No Java sources found')
def quote(path):
 value=str(path)
 if '\n' in value or '\r' in value:raise SystemExit('Newlines in Java source paths are unsupported')
 return '"'+value.replace('\\','\\\\').replace('"','\\"')+'"'
Path('build/sources.args').write_text('\n'.join(map(quote,sources))+'\n')
PY
"$ANDROID_BUILD_TOOLS/aapt2" compile --dir app/src/main/res -o build/compiled/resources.zip
"$ANDROID_BUILD_TOOLS/aapt2" link -o build/base.apk --manifest app/src/main/AndroidManifest.xml -I "$ANDROID_PLATFORM_JAR" -A app/src/main/assets build/compiled/resources.zip
java -jar "$ECJ_JAR" -8 -bootclasspath "$ANDROID_PLATFORM_JAR:$ANDROID_BUILD_TOOLS/core-lambda-stubs.jar" -d build/classes @build/sources.args
# One archive keeps D8 a single invocation even with very large class sets.
python3 - <<'PY'
from pathlib import Path
import zipfile
classes=sorted(Path('build/classes').rglob('*.class'))
if not classes:raise SystemExit('Java compilation produced no classes')
with zipfile.ZipFile('build/classes.jar','w') as jar:
 for path in classes:
  entry=zipfile.ZipInfo(path.relative_to('build/classes').as_posix(),(1980,1,1,0,0,0))
  entry.external_attr=0o644 << 16
  jar.writestr(entry,path.read_bytes())
PY
"$ANDROID_BUILD_TOOLS/d8" --release --lib "$ANDROID_PLATFORM_JAR" --min-api "$min_api" --output build/dex build/classes.jar
python3 - <<'PY'
import zipfile
from pathlib import Path
files=sorted(Path('build/dex').glob('*.dex'))
if not files:raise SystemExit('D8 produced no DEX files')
with zipfile.ZipFile('build/base.apk','a') as z:
 for path in files:
  entry=zipfile.ZipInfo(path.name,(1980,1,1,0,0,0));entry.external_attr=0o644 << 16
  z.writestr(entry,path.read_bytes())
PY
"$ANDROID_BUILD_TOOLS/zipalign" -f -p 4 build/base.apk build/aligned.apk
if [[ -n "${APK_KEYSTORE:-}" ]]; then
 # A separate private-key password is optional; never expose either secret in argv.
 export APK_KEY_PASSWORD="${APK_KEY_PASSWORD:-$APK_KEYSTORE_PASSWORD}"
 "$ANDROID_BUILD_TOOLS/apksigner" sign --ks "$APK_KEYSTORE" --ks-key-alias "${APK_KEY_ALIAS:-form-strength}" --ks-pass env:APK_KEYSTORE_PASSWORD --key-pass env:APK_KEY_PASSWORD --out build/release.apk build/aligned.apk
 "$ANDROID_BUILD_TOOLS/apksigner" verify --verbose --print-certs build/release.apk
else
 cp build/aligned.apk build/release.apk
 echo 'Unsigned APK built. Set APK_KEYSTORE and APK_KEYSTORE_PASSWORD for an installable release.'
fi
"$ANDROID_BUILD_TOOLS/zipalign" -c -p 4 build/release.apk
mv -- build/release.apk "$release_path"
printf 'Release artifact: %s\n' "$release_path"
