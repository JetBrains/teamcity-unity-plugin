import org.jetbrains.changelog.Changelog

plugins {
    id("io.github.rodm.teamcity-server") version "1.5"
    id("org.jetbrains.changelog") version "2.0.0"
    id("plugin.common")
}

changelog {
    path.set(file("../CHANGELOG.md").canonicalPath)
    groups.set(listOf("Added", "Changed", "Fixed"))
}

teamcity {
    version = "2023.07-SNAPSHOT"
    allowSnapshotVersions = true

    server {
        descriptor {
            name = "unity"
            displayName = "Unity Support"
            description = "Provides build facilities for Unity projects"
            version = project.version.toString()
            vendorName = "JetBrains"
            vendorUrl = "https://www.jetbrains.com/"

            useSeparateClassloader = true
            allowRuntimeReload = true

            // new agent API is used since that version
            minimumBuild = "116751" // 2022.10
        }

        publish {
            token = project.findProperty("jetbrains.marketplace.token").toString()
            notes = changelog.renderItem(changelog.getLatest(), Changelog.OutputType.HTML)
        }

        archiveName = rootProject.name
    }
}

dependencies {
    agent(project(path = ":plugin-unity-agent", configuration = "plugin"))

    implementation(project(":plugin-unity-common"))
    implementation((project(":csharp-parser"))) {
        exclude(group = "com.ibm.icu")
    }
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.21")
    provided("org.jetbrains.teamcity.internal:server:2023.07-SNAPSHOT")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.6.2")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

tasks.register("getLatestChangelogVersion") {
    print(changelog.getLatest().version)
}