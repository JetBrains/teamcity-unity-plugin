

plugins {
    id("io.github.rodm.teamcity-common") version "1.5"
    id("plugin.common")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.21")
    implementation("com.vdurmont:semver4j:3.1.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.6.2")
}

teamcity {
    version = "2023.07-SNAPSHOT"
    allowSnapshotVersions = true
}
