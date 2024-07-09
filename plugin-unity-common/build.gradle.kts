plugins {
    alias(libs.plugins.teamcity.common)
    id("plugin.teamcity.common")
    id("plugin.common")
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.semver4j)

    constraints {
        implementation(libs.constraint.transitive.icu4j) {
            because("previous versions have faulty jar files which cause problems during incremental compilation (which is enabled by default since Kotlin 1.8.20)")
        }
    }

    testImplementation(libs.kotlin.kotest)
    testImplementation(libs.kotest.assertions)
}
