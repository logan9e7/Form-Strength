#!/usr/bin/env bash
# Build a signed Play upload bundle without introducing Gradle.
set -euo pipefail
cd "$(dirname "$0")/.."
: "${BUNDLETOOL_JAR:?Set BUNDLETOOL_JAR to the official bundletool-all jar}"
: "${APK_KEYSTORE:?Set APK_KEYSTORE to the existing release keystore}"
: "${APK_KEYSTORE_PASSWORD:?Set APK_KEYSTORE_PASSWORD securely}"
: "${ANDROID_BUILD_TOOLS:?Set ANDROID_BUILD_TOOLS}"
export APK_KEY_PASSWORD="${APK_KEY_PASSWORD:-$APK_KEYSTORE_PASSWORD}"
export APK_KEY_ALIAS="${APK_KEY_ALIAS:-form-strength}"
release_name=$(python3 - <<'PY'
import re,xml.etree.ElementTree as ET
root=ET.parse('app/src/main/AndroidManifest.xml').getroot()
a='{http://schemas.android.com/apk/res/android}'
version=root.get(a+'versionName','')
if not re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9._-]{0,63}',version):
 raise SystemExit('Invalid release version')
if int(root.find('uses-sdk').get(a+'targetSdkVersion','0'))<36:
 raise SystemExit('Play candidate must target API 36 or newer')
print('Form-Strength-'+version)
PY
)
mkdir -p outputs
bundle="outputs/$release_name.aab"
rm -f -- "$bundle" "$bundle.sha256"
# A failed build never leaves a candidate at its final output path.
bash scripts/build.sh
mkdir -p build/bundle
rm -f build/bundle/resources.apk build/bundle/base.zip build/bundle/signed.aab
"${AAPT2_BUNDLE:-$ANDROID_BUILD_TOOLS/aapt2}" link --proto-format \
 -o build/bundle/resources.apk --manifest app/src/main/AndroidManifest.xml \
 -I "$ANDROID_PLATFORM_JAR" -A app/src/main/assets build/compiled/resources.zip
python3 - <<'PY'
from pathlib import Path
import zipfile
with zipfile.ZipFile('build/bundle/resources.apk') as resources, zipfile.ZipFile('build/bundle/base.zip','w',zipfile.ZIP_DEFLATED) as module:
 for name in resources.namelist():
  if name.endswith('/'):continue
  if name=='AndroidManifest.xml':target='manifest/'+name
  elif name=='resources.pb' or name.startswith(('res/','assets/','lib/')):target=name
  else:raise SystemExit('Unexpected resource package entry: '+name)
  module.writestr(target,resources.read(name))
 dex=sorted(Path('build/dex').glob('classes*.dex'))
 if not dex:raise SystemExit('Missing DEX files')
 for path in dex:module.write(path,'dex/'+path.name)
PY
java -jar "$BUNDLETOOL_JAR" build-bundle --modules=build/bundle/base.zip --output=build/bundle/signed.aab
java -m jdk.jartool/sun.security.tools.jarsigner.Main -keystore "$APK_KEYSTORE" -storepass:env APK_KEYSTORE_PASSWORD \
 -keypass:env APK_KEY_PASSWORD -sigalg SHA256withRSA -digestalg SHA-256 \
 build/bundle/signed.aab "$APK_KEY_ALIAS"
java -jar "$BUNDLETOOL_JAR" validate --bundle=build/bundle/signed.aab
# Verify against the trusted signing identity, not the system CA trust store.
java -m jdk.jartool/sun.security.tools.jarsigner.Main -verify -strict -keystore "$APK_KEYSTORE" \
 -storepass:env APK_KEYSTORE_PASSWORD build/bundle/signed.aab "$APK_KEY_ALIAS"
mv build/bundle/signed.aab "$bundle"
sha256sum "$bundle" > "$bundle.sha256"
printf 'Signed Play upload candidate: %s\n' "$bundle"
