import org.gradle.kotlin.dsl.kotlin
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

project.group = "teamcity-unity-plugin"
project.version = if (project.findProperty("version") == "unspecified") {
    "SNAPSHOT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYYMMddHHmmss"))
} else {
    project.version
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
