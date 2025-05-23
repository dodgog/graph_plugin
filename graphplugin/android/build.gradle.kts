plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("app.cash.sqldelight")
}

allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

group = "com.daylightcomputer.graphplugin"
version = "1.0"

android {
    namespace = "com.daylightcomputer.graphplugin"
    compileSdk = 35

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    defaultConfig {
        minSdk = 16
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName.set(
                "com.daylightcomputer.graphplugin.database.sqldefinitions",
            )
        }
    }
}

dependencies {
    implementation("com.daylightcomputer.hlc:hlc:1.0-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("app.cash.sqldelight:android-driver:2.0.2")

    testImplementation("androidx.test:core:1.6.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")
    testImplementation("app.cash.sqldelight:sqlite-driver:2.0.2")
    testImplementation("org.testng:testng:6.9.6")
    testImplementation("com.willowtreeapps.assertk:assertk:0.28.1")

    testImplementation("org.xerial:sqlite-jdbc") {
        version { strictly("3.32.3.3") }
    }
}
