# Building Calculator

Install Android Studio with Android SDK platform 35, Android Build Tools 35, Java 17 and Gradle 8.11.1.

Open the root project in Android Studio and sync Gradle. Run the app module on an emulator or device (API 24+).

Command-line CI equivalent (with Gradle 8.11.1 available):

```sh
gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

Build output: `app/build/outputs/apk/debug/app-debug.apk`.

Release builds are unsigned unless signing is configured securely outside this repository. Never commit private signing credentials.

The repository includes a Gradle wrapper properties file but does not yet contain the executable wrapper JAR; CI invokes a version-pinned Gradle distribution directly.

## Verification targets

Functional tests cover expression precedence, factorials, percentages, trigonometry, currency-model safety, loan math and unit conversions. Before production rollout, run instrumented interaction tests, automated screenshot-difference tests at multiple display densities, accessibility checks, process-death restoration, landscape tests and Play pre-launch reports.

The vector files in `branding/screens/` are design references; screen-perfect matches must be assessed using captured emulator screenshots rather than inferred from the design source.
