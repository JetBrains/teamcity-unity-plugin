plugins {
    id("io.github.rodm.teamcity-agent") version "1.5"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.21"
    kotlin("jvm") version "1.7.21"
}

teamcity {
    version = "2023.07-SNAPSHOT"
    allowSnapshotVersions = true

    agent {
        descriptor {
            pluginDeployment {
                useSeparateClassloader = true
                executableFiles {
                    include("tools/unity-environment-detector.sh")
                    include("tools/unity-environment-detector.bat")
                }
            }
        }

        archiveName = "teamcity-unity-agent"
    }

    files {
        into("tools") {
            from("tools/pe-product-version-detector/binaries/PeProductVersionDetector.exe")
            from("tools/unity-environment-detector/unity-environment-detector.sh")
            from("tools/unity-environment-detector/unity-environment-detector.bat")
        }
    }
}

dependencies {
    implementation(project(":plugin-unity-common"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.21")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("com.vdurmont:semver4j:3.1.0")
    implementation("commons-configuration:commons-configuration:1.10")
    implementation("commons-io:commons-io:2.11.0")
    provided("org.jetbrains.teamcity.internal:agent:2023.07-SNAPSHOT")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.7.21")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.testng:testng:7.5")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.6.2")
}