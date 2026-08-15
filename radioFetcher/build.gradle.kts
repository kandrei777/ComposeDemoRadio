plugins {
    `java-library`
    kotlin("jvm")
}

java {
    // Android Studio и AGP 8.13+ отлично работают с Java 21.
    // Главное: убедитесь, что в модуле :app в compileOptions тоже стоит VERSION_21!
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(libs.jsoup)
    implementation(libs.gson)
}