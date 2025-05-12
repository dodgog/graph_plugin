# kotlin plugin

./gradlew :graphplugin:testDebugUnitTest makes a build folder in the anroid 
folder and runs the unit tests. ./gradlew tasks lists all the tasks

The command to regenerate JNI bindings is:
```
flutter pub run jnigen --config jnigen.yaml # run from graphplugin project root 
```

The `example/` app must be built at least once in _release_ mode (
eg `flutter build apk`) before running jnigen.
This is the equivalent of Gradle Sync in Android Studio, and enables `jnigen` to
run a Gradle stub and determine release build's classpath, which contains the
paths to relevant dependencies.
Therefore a build must have been run after cleaning build directories, or
updating Java dependencies.

This is a known complexity of the Gradle build system, and if you know a
solution, please contribute to issue discussion at #33 as the dart people 
requested in the original readme.

Note that `jnigen.yaml` of this example contains the
option `suspend_fun_to_async: true`. This will generate `async` method bindings
from Kotlin's `suspend fun`s.

## Set up the plugin with IntelliSense support

Following the default steps provided by the Dart Native Manual is
not sufficient in order to get full Kotlin support in the IDE.
You actually discover soon enough that the Kotlin code that you end up
converting into Dart bindings with JNI is not syntax highlighted.

In order to solve that, I have found a workaround. It involves setting up a
root-level Gradle settings file which in turn declares the main plugins,
projects and flutter properties.
It doesn't seem like the correct way to do it, and must be reviewed by a 
person with gradle experience.

The starting point should either be 
`flutter create --template=plugin_ffi --platform=android kotlin_plugin` or the 
example kotlin plugin in the dart native repo
https://github.com/dart-lang/native/tree/79df0dcfb27265f669229530f9d2fbaa712fd735/pkgs/jnigen/example/kotlin_plugin
you shoudl check in case there is a newer example which solves the issue.

Add to `android/build.gradle`
```groovy
// Add local.properties
def localProperties = new Properties()
def localPropertiesFile = rootProject.file('local.properties')
if (localPropertiesFile.exists()) {
    localPropertiesFile.withReader('UTF-8') { reader ->
        localProperties.load(reader)
    }
}

// Apply the kotlin-android plugin! 
apply plugin: 'kotlin-android'

// Add source sets
sourceSets {
    main.java.srcDirs += 'src/main/kotlin'
}

//Also make sure that kotlin and java target the same version:

compileOptions {
    sourceCompatibility JavaVersion.VERSION_11
    targetCompatibility JavaVersion.VERSION_11
}

kotlinOptions {
    jvmTarget = "11"
}
```

Delete the android/settings.gradle file (optionally, because it only names 
the root project).

Update the version of kotlin android plugin in example/android/settings.gradle
```groovy
- id "org.jetbrains.kotlin.android" version "1.8.10" apply false
+ id "org.jetbrains.kotlin.android" version "1.9.24" apply false
```

Add the root folder settings.gradle file
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

Soft link or recursively copy the example/android/gradle and 
example/android/local.properties into root folder

Create a root gradle.properties file
```groovy
android.enableJetifier=true
android.useAndroidX=true
```

Remember to rename the package name everywhere to your com.name.app



