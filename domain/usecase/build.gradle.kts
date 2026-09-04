// domain/usecase - UseCases

plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.kapt")
}

apply(plugin: "dagger.hilt.android.plugin")
// UseCases فقط interface هستند و نیاز به DI annotation ندارند در سطح domain

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    api(project(":domain:model"))
    api(project(":domain:repository"))
    implementation("javax.inject:javax.inject:1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
}