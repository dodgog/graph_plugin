#!/bin/bash

cd coreplugin/android

RUNTIME=$(./gradlew -q printRuntimeClasspath)

java -classpath "build/tmp/kotlin-classes/debug:$RUNTIME" \
     com.daylightcomputer.coreplugin.cli.DocumentCLI
