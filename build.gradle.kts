import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm") version "1.8.0"
    `kotlin-dsl`
    id("com.vanniktech.maven.publish") version("0.18.0")
}

group = "io.github.neas-neas"
version = "0.0.3"

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

pluginManager.withPlugin("com.vanniktech.maven.publish") {
    mavenPublish {
        sonatypeHost = SonatypeHost.S01
    }
}