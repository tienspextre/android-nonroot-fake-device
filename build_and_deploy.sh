#!/bin/bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "=== 1. Compiling DeviceIDManager ==="
mkdir -p "$DIR/devid_pro/bin"
javac -source 8 -target 8 \
  -cp /home/vorza/.gradle/caches/9.4.1/transforms/101202072ba200363ad9753ebbf43c40/transformed/android.jar \
  -d "$DIR/devid_pro/bin" \
  "$DIR/devid_pro/src/com/sec/deviceid/MainActivity.java"

echo "=== 2. Converting to DEX ==="
java -cp "$DIR/r8.jar" com.android.tools.r8.D8 --min-api 21 \
  --output "$DIR/devid_pro/" "$DIR/devid_pro/bin/com/sec/deviceid/"*.class

echo "=== 3. Packaging APK with Apktool ==="
rm -rf "$DIR/devid_pro/smali"
java -jar "$DIR/baksmali.jar" d "$DIR/devid_pro/classes.dex" -o "$DIR/devid_pro/smali"
java -jar "$DIR/apktool.jar" b "$DIR/devid_pro" -o "$DIR/devid_pro/DeviceIDManager_unsigned.apk"

echo "=== 4. Signing with Platform Testkey ==="
mkdir -p "$DIR/devid_pro/dist"
java -jar "$DIR/uber-apk-signer.jar" \
  -a "$DIR/devid_pro/DeviceIDManager_unsigned.apk" \
  --ks "$DIR/platform.jks" \
  --ksPass android \
  --ksAlias platform \
  --ksKeyPass android \
  --allowResign \
  --out "$DIR/devid_pro/dist"

cp "$DIR/devid_pro/dist/DeviceIDManager_unsigned-aligned-signed.apk" "$DIR/DeviceIDManager_v3.apk"
echo "=== Hoàn tất! File APK sẵn sàng tại: $DIR/DeviceIDManager_v3.apk ==="
