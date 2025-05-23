# Graph Plugin for Flutter

## Getting Started

Warning: AI USE Formatted by AI, written by Vania

### Running Tests
```bash
# Run unit tests (creates a build folder in the Android directory)
./gradlew :graphplugin:testDebugUnitTest

# List all available Gradle tasks
./gradlew tasks
```

### JNI Bindings

To regenerate JNI bindings:
```bash
# Run from graphplugin project root
flutter pub run jnigen --config jnigen.yaml
```

**Important Prerequisites:**

1. Build the `example/` app at least once in **release mode** (
   e.g., `flutter build apk`) before running jnigen
2. This is equivalent to Gradle Sync in Android Studio
3. This enables `jnigen` to run a Gradle stub and determine the release build's
   classpath
4. A build must be run after cleaning build directories or updating Java
   dependencies

> **Note:** This is a known complexity of the Gradle build system. If you know a
> solution, please contribute to issue discussion at #33 as requested by the Dart
> team in the original readme.

The `jnigen.yaml` file contains the option `suspend_fun_to_async: true`, which
generates `async` method bindings from Kotlin's `suspend fun`s.

# Interactive mode (asks for confirmation at each step)
./knowledge_sync_build.sh

# Silent mode (runs everything automatically)
./knowledge_sync_build.sh --accept
