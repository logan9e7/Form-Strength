#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${ANDROID_PLATFORM_JAR:?}" "${ANDROID_BUILD_TOOLS:?}" "${ECJ_JAR:?}"
: "${APK_KEYSTORE:?Use the release signer for instrumentation}" "${APK_KEYSTORE_PASSWORD:?}"
mkdir -p build/android-os-qa/classes build/android-os-qa/dex
rm -f build/android-os-qa/classes/com/logan/formstrength/qa/*.class build/android-os-qa/dex/*.dex
"$ANDROID_BUILD_TOOLS/aapt2" link -o build/android-os-qa/base.apk --manifest tests/android/AndroidManifest.xml -I "$ANDROID_PLATFORM_JAR"
java -jar "$ECJ_JAR" -8 -cp "$ANDROID_PLATFORM_JAR" -d build/android-os-qa/classes tests/android/OsGateInstrumentation.java
python3 - <<'PYJAR'
import pathlib,zipfile
with zipfile.ZipFile('build/android-os-qa/classes.jar','w') as z:
 for p in pathlib.Path('build/android-os-qa/classes').rglob('*.class'):z.write(p,p.relative_to('build/android-os-qa/classes'))
PYJAR
"$ANDROID_BUILD_TOOLS/d8" --release --lib "$ANDROID_PLATFORM_JAR" --min-api 26 --output build/android-os-qa/dex build/android-os-qa/classes.jar
python3 - <<'PY'
import zipfile,pathlib
with zipfile.ZipFile('build/android-os-qa/base.apk','a') as z:
 for p in pathlib.Path('build/android-os-qa/dex').glob('*.dex'):z.write(p,p.name)
PY
"$ANDROID_BUILD_TOOLS/zipalign" -f 4 build/android-os-qa/base.apk build/android-os-qa/aligned.apk
export APK_KEY_PASSWORD="${APK_KEY_PASSWORD:-$APK_KEYSTORE_PASSWORD}"
"$ANDROID_BUILD_TOOLS/apksigner" sign --ks "$APK_KEYSTORE" --ks-key-alias "${APK_KEY_ALIAS:-form-strength}" --ks-pass env:APK_KEYSTORE_PASSWORD --key-pass env:APK_KEY_PASSWORD --out build/android-os-qa/qa.apk build/android-os-qa/aligned.apk
"$ANDROID_BUILD_TOOLS/apksigner" verify build/android-os-qa/qa.apk
printf 'Built same-signer test APK: build/android-os-qa/qa.apk\n'
