plugins {
    alias(libs.plugins.teamcity.common)
    id("plugin.teamcity.common")
    id("plugin.common")
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.semver4j)

    testImplementation(libs.kotlin.kotest)
    testImplementation(libs.kotest.assertions)
}
