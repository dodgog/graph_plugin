plugins {
    kotlin("jvm") version "1.8.21"
    application
    `maven-publish`
}

group = "com.hlc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // JUnit 5 for testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("com.willowtreeapps.assertk:assertk:0.28.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.hlc.demo.HLCDemoKt")
}

// Thin jar which doesn't bundle dependencies
tasks.jar {
    // Configuration can go here if needed
}

// Fat jar which bundles dependencies
tasks.register<Jar>("fatJar") {
    manifest {
        attributes("Main-Class" to "com.hlc.demo.HLCDemoKt")
    }
    archiveClassifier.set("fat")
    from(
        configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        },
    )
    with(tasks.jar.get())
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            groupId = "com.daylightcomputer.hlc"
            artifactId = "hlc"
            version = "1.0-SNAPSHOT"
        }
    }
}
