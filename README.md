![Sentry](./docs/assets/logo.svg)


## Sentry
A lightweight wrapper for Android Permissions.

[![Kotlin](https://img.shields.io/badge/Kotlin-1.3.11-blue.svg?style=flat-square)](http://kotlinlang.org)
[![Build Status](https://img.shields.io/travis/Karn/sentry.svg?style=flat-square)](https://travis-ci.org/Karn/sentry)
[![Codecov](https://img.shields.io/codecov/c/github/karn/sentry.svg?style=flat-square)](https://codecov.io/gh/Karn/sentry)
[![GitHub (pre-)release](https://img.shields.io/github/release/karn/sentry/all.svg?style=flat-square)
](./../../releases)

#### GETTING STARTED
Sentry (pre-)releases are available via JitPack. It is recommended that a specific release version is selected when using the library in production as there may be breaking changes at anytime.

> **Tip:** Test out the canary channel to try out features by using the latest develop snapshot; `develop-SNAPSHOT`.

```Groovy
// Project level build.gradle
// ...
repositories {
    maven { url 'https://jitpack.io' }
}
// ...

// Module level build.gradle
dependencies {
    // Replace version with release version, e.g. 1.0.0-alpha, -SNAPSHOT
    implementation "io.karn:sentry:[VERSION]"
}
```


#### USAGE
The most basic case is as follows:

```diff
// Add the following delegate to your activity.
- class MyActivity : AppCompatActivity() {
+ class MyActivity : AppCompatActivity(), Permissions by SentryPermissionHandler {

// or optionally manually delegate to the SentryPermissionHandler by adding the following override
// in your Activity
+    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
+        SentryPermissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults)
+    }
}
```

Then anywhere in your activity you can make a request to fetch a permission.

```Kotlin
Sentry
    // A reference to your current activity.
    .with(activity)
    // The permission that is being queried.
    .requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) { isGranted: Boolean ->
        Log.v("Sentry", "Granted permission to write to external storage? $isGranted")
    }
```

#### CONTRIBUTING
There are many ways to [contribute](./.github/CONTRIBUTING.md), you can
- submit bugs,
- help track issues,
- review code changes.
