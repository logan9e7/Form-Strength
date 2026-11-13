#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${ANDROID_PLATFORM_JAR:?Set ANDROID_PLATFORM_JAR to android.jar}"
: "${ANDROID_BUILD_TOOLS:?Set ANDROID_BUILD_TOOLS}"
: "${ECJ_JAR:?Set ECJ_JAR}"
mkdir -p build/classes build/dex build/compiled outputs
"$ANDROID_BUILD_TOOLS/aapt2" compile --dir app/src/main/res -o build/compiled/resources.zip
"$ANDROID_BUILD_TOOLS/aapt2" link -o build/base.apk --manifest app/src/main/AndroidManifest.xml -I "$ANDROID_PLATFORM_JAR" -A app/src/main/assets build/compiled/resources.zip
java -jar "$ECJ_JAR" -8 -bootclasspath "$ANDROID_PLATFORM_JAR:$ANDROID_BUILD_TOOLS/core-lambda-stubs.jar" -d build/classes $(find app/src/main/java -name '*.java' | sort)
find build/classes -name '*.class' -print0 | sort -z | xargs -0 "$ANDROID_BUILD_TOOLS/d8" --release --lib "$ANDROID_PLATFORM_JAR" --min-api 26 --output build/dex
python3 - <<'PY'
import zipfile
from pathlib import Path
with zipfile.ZipFile('build/base.apk','a') as z:
 for file in Path('build/dex').glob('*.dex'):z.write(file,file.name)
PY
"$ANDROID_BUILD_TOOLS/zipalign" -f -p 4 build/base.apk build/aligned.apk
if [[ -n "${APK_KEYSTORE:-}" ]]; then
 : "${APK_KEYSTORE_PASSWORD:?Set signing password in environment}"
 "$ANDROID_BUILD_TOOLS/apksigner" sign --ks "$APK_KEYSTORE" --ks-key-alias form-strength --ks-pass env:APK_KEYSTORE_PASSWORD --key-pass env:APK_KEYSTORE_PASSWORD --out outputs/Form-Strength-1.5.1.apk build/aligned.apk
 "$ANDROID_BUILD_TOOLS/apksigner" verify --verbose --print-certs outputs/Form-Strength-1.5.1.apk
else
 cp build/aligned.apk outputs/Form-Strength-unsigned.apk
 echo 'Unsigned APK built. Set APK_KEYSTORE and APK_KEYSTORE_PASSWORD for an installable release.'
fi
