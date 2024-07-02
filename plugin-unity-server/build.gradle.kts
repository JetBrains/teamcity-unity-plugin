import org.jetbrains.changelog.Changelog

plugins {
    alias(libs.plugins.teamcity.server)
    alias(libs.plugins.changelog)
    id("plugin.teamcity.common")
    id("plugin.common")
}

changelog {
    path.set(file("../CHANGELOG.md").canonicalPath)
    groups.set(listOf("Added", "Changed", "Fixed"))
}

teamcity {
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

        // preserving the old archive name
        archiveName = project.name
    }
}

dependencies {
    agent(project(path = ":plugin-unity-agent", configuration = "plugin"))

    implementation(project(":plugin-unity-common"))
    implementation((project(":csharp-parser"))) {
        exclude(group = "com.ibm.icu")
    }
    implementation(libs.kotlin.stdlib)
    provided(libs.teamcity.internal.server)

    constraints {
        implementation(libs.constraint.transitive.icu4j) {
            because("previous versions have faulty jar files which cause problems during incremental compilation (which is enabled by default since Kotlin 1.8.20)")
        }
    }

    testImplementation(libs.kotlin.kotest)
    testImplementation(libs.mockk)
    testImplementation(libs.kotest.assertions)
}

tasks.register("getLatestChangelogVersion") {
    print(changelog.getLatest().version)
}
