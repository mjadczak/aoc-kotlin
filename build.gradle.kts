plugins {
    application
    kotlin("jvm") version "2.2.21"
}

group = "uk.co.mjdk"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.4.0")
    implementation("org.apache.commons:commons-math3:3.6.1")
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    compilerOptions {
        progressiveMode = true
    }
}
