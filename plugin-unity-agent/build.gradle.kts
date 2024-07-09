plugins {
    alias(libs.plugins.teamcity.agent)
    alias(libs.plugins.kotlin.serialization)
    id("plugin.teamcity.common")
    id("plugin.common")
}

teamcity {
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

        // preserving the old archive name
        archiveName = "teamcity-unity-agent"

        files {
            into("tools") {
                from("tools/pe-product-version-detector/binaries/PeProductVersionDetector.exe")
                from("tools/unity-environment-detector/unity-environment-detector.sh")
                from("tools/unity-environment-detector/unity-environment-detector.bat")
            }
        }
    }
}

dependencies {
    implementation(project(":plugin-unity-common"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.semver4j)
    implementation(libs.commons.configuration)
    implementation(libs.commons.io)
    provided(libs.teamcity.internal.agent)

    constraints {
        implementation(libs.constraint.transitive.icu4j) {
            because("previous versions have faulty jar files which cause problems during incremental compilation (which is enabled by default since Kotlin 1.8.20)")
        }
    }

    testImplementation(libs.kotlin.kotest)
    testImplementation(libs.mockk)
    testImplementation(libs.testng)
    testImplementation(libs.kotest.assertions)
}
