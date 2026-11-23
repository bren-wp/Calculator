# Calculator — build & release

The native application targets API 36, minSdk 24 and uses Java 17, AGP 8.13.2, Kotlin 2.3.21, Gradle 8.13 and stable AndroidX dependencies.

Open the project in Android Studio or run:

```bash
gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:bundleDebug :app:bundleRelease
```

Android CI attaches **Calculator-debug.apk**, **Calculator-debug.aab**, **Calculator-release-UNSIGNED.aab**, **Calculator-source.zip** and **SHA256SUMS.txt** on every passing PR build.

Pushes to main or the development branch create a tagged GitHub **pre-release** with these assets. The upload signature used for debug artifacts is NOT a production key. The release bundle is **unsigned** and cannot be uploaded to Google Play until signed with the account's secure upload key. Do not commit private keys or include them in source archives.

A successful build does not prove pixel-perfect parity. Visual QA compares 26 captured screen renders against reference SVGs and emits a per-screen difference report. Release readiness additionally requires Play Console pre-launch checks, signing, privacy declarations, policy review and manual device and accessibility QA.
