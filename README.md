# Tokimeite Nabi

This repository is set up as a native Android application project suitable for building a Google Play App Bundle (AAB).

## Project structure

- `app/` - Android app module
- `app/src/main/AndroidManifest.xml` - app manifest
- `app/src/main/java/.../MainActivity.kt` - main activity
- `app/src/main/res/` - app resources

## Requirements

To build the release AAB, install:

- Android Studio
- JDK 17
- Android SDK with API 34

## Build an AAB

Open the project in Android Studio, or from a terminal:

```bash
./gradlew bundleRelease
```

The generated AAB will be in:

```text
app/build/outputs/bundle/release/
```

## Notes

This project is intentionally kept minimal and ready for Google Play upload as a signed release bundle.
