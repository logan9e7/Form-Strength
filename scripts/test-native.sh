#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${ANDROID_PLATFORM_JAR:?Set ANDROID_PLATFORM_JAR}"
: "${ECJ_JAR:?Set ECJ_JAR}"
: "${JSON_JAR:?Set JSON_JAR to org.json 20240303 jar}"
mkdir -p build/test-classes
java -jar "$ECJ_JAR" -8 -cp "$JSON_JAR:$ANDROID_PLATFORM_JAR" -d build/test-classes app/src/main/java/com/logan/formstrength/LocalStore.java app/src/main/java/com/logan/formstrength/GeminiMealClient.java app/src/main/java/com/logan/formstrength/HealthConnection.java tests/ValidationTest.java
java -cp "build/test-classes:$JSON_JAR:$ANDROID_PLATFORM_JAR" com.logan.formstrength.ValidationTest
