import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.21"
}

repositories {
    mavenCentral()
}

project.version = if (project.findProperty("version") == "unspecified") {
    "SNAPSHOT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYYMMddHHmmss"))
} else {
    project.version
}

val teamcityVersion = project.property("teamcityVersion")

val group = "teamcity-unity-plugin"

allprojects {
    group = group
    version = project.version
}

subprojects {
    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
    }

    tasks {
        compileKotlin {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }

        test {
            useTestNG()
        }
    }
}