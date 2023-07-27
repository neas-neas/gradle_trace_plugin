plugins {
    kotlin("jvm") version "1.8.0"
    `kotlin-dsl`
    `maven-publish`
}

group = "com.neas"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

gradlePlugin {
    plugins {
        register("build-trace") {
            id = "build.trace"
            implementationClass = "com.neas.trace.BuildTracePlugin"
        }
    }
}