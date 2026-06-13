#!/bin/bash
set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"
SDK="$HOME/Android/Sdk"
BT="$SDK/build-tools/35.0.0"
PLATFORM="$SDK/platforms/android-34/android.jar"
KS="$ROOT/lockscreenpin-release.jks"

cd "$ROOT"
rm -rf bin obj gen
mkdir -p bin obj gen

echo "==> Compiling resources..."
"$BT/aapt2" compile --dir res -o gen/res.zip

echo "==> Linking resources..."
"$BT/aapt2" link gen/res.zip \
  -I "$PLATFORM" \
  --manifest AndroidManifest.xml \
  --java gen \
  --min-sdk-version 26 \
  --target-sdk-version 34 \
  --version-code 2 \
  --version-name "2.0" \
  -o bin/resources.apk

echo "==> Compiling Java..."
javac -source 8 -target 8 \
  -classpath "$PLATFORM" \
  -bootclasspath "$PLATFORM" \
  -d obj \
  src/dev/pinkeys/lockscreenpin/*.java \
  gen/dev/pinkeys/lockscreenpin/R.java

echo "==> Converting to dex..."
"$BT/d8" obj/dev/pinkeys/lockscreenpin/*.class \
  --release \
  --min-api 26 \
  --output bin/

echo "==> Assembling APK..."
cp bin/resources.apk bin/lockscreenpin-unsigned.apk
cd bin
zip -j lockscreenpin-unsigned.apk classes.dex
cd ..

echo "==> Aligning..."
"$BT/zipalign" -f 4 bin/lockscreenpin-unsigned.apk bin/lockscreenpin-aligned.apk

echo "==> Signing..."
"$BT/apksigner" sign \
  --ks "$KS" \
  --ks-pass pass:pinkeys2024 \
  --key-pass pass:pinkeys2024 \
  --ks-key-alias lockscreenpin \
  --out "$ROOT/lockscreenpin.apk" \
  bin/lockscreenpin-aligned.apk

echo ""
echo "Done: $(ls -lh "$ROOT/lockscreenpin.apk" | awk '{print $5, $NF}')"
