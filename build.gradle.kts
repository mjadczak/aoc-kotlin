plugins {
    application
    kotlin("jvm") version "1.7.21"
}

group = "uk.co.mjdk"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
}
