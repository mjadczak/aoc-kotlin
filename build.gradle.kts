plugins {
    application
    kotlin("jvm") version "1.9.21"
}

group = "uk.co.mjdk"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.6")
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
