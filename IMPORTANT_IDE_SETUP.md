# Setting Up IntelliSense Support

The default setup from Dart Native Manual doesn't provide full Kotlin support in
IDEs. Without proper configuration, Kotlin code used for Dart bindings with JNI
won't have syntax highlighting.

## Workaround for IDE Support

This workaround involves setting up a root-level Gradle settings file to declare
plugins, projects, and Flutter properties. Note that this approach should be
reviewed by someone with Gradle experience.

### Initial Project Setup

Start with either:
- `flutter create --template=plugin_ffi --platform=android kotlin_plugin`
- The example Kotlin plugin in the Dart Native repo: [example/kotlin_plugin](https://github.com/dart-lang/native/tree/79df0dcfb27265f669229530f9d2fbaa712fd735/pkgs/jnigen/example/kotlin_plugin)

Check if a newer example exists that resolves this issue.

### Configuration Steps

1. **Update `android/build.gradle`**:
```groovy
// Add local.properties
def localProperties = new Properties()
def localPropertiesFile = rootProject.file('local.properties')
if (localPropertiesFile.exists()) {
    localPropertiesFile.withReader('UTF-8') { reader ->
        localProperties.load(reader)
    }
}

// Apply the kotlin-android plugin
apply plugin: 'kotlin-android'

// Add source sets
sourceSets {
    main.java.srcDirs += 'src/main/kotlin'
}

// Make sure Kotlin and Java target the same version
compileOptions {
    sourceCompatibility JavaVersion.VERSION_11
    targetCompatibility JavaVersion.VERSION_11
}

kotlinOptions {
    jvmTarget = "11"
}
```

2. **Delete `android/settings.gradle`** (optional, as it only names the root
   project)

3. **Update Kotlin plugin version in `example/android/settings.gradle`**:
```groovy
- id "org.jetbrains.kotlin.android" version "1.8.10" apply false
+ id "org.jetbrains.kotlin.android" version "1.9.24" apply false
```

4. **Create a root-level `settings.gradle` file**:
```groovy
pluginManagement {
    // Re-use the block that Flutter puts in example/android/settings.gradle
    def props = new Properties()
    file("example/android/local.properties").withInputStream { props.load(it) }
    includeBuild("${props['flutter.sdk']}/packages/flutter_tools/gradle")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id "dev.flutter.flutter-plugin-loader" version "1.0.0"
    id "com.android.application" version "8.6.0" apply false
    id "org.jetbrains.kotlin.android" version "1.9.24" apply false
}

/* --- declare native modules ----------------------- */
include(":graphplugin")
project(":graphplugin").projectDir = file("android")

include(":exampleApp")
project(":exampleApp").projectDir = file("example/android/app")

rootProject.name = 'graphplugin'
```

5. **Copy Gradle files to the root folder**:
   - Soft link or recursively copy `example/android/gradle`
     and `example/android/local.properties` into root folder

6. **Create a root `gradle.properties` file**:
```groovy
android.enableJetifier=true
android.useAndroidX=true
```

7. **Important**: Remember to rename the package name everywhere to
   your `com.name.app`


