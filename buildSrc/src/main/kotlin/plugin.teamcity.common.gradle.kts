import com.github.rodm.teamcity.ValidationMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("io.github.rodm.teamcity-base")
    id("com.diffplug.spotless")
}

project.group = "teamcity-unity-plugin"
project.version = if (project.findProperty("version") == "unspecified") {
    "SNAPSHOT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYYMMddHHmmss"))
} else {
    project.version
}

teamcity {
    // dirty hack
    // https://github.com/gradle/gradle/issues/15383
    version = extensions.getByType<VersionCatalogsExtension>()
        .named("libs")
        .findVersion("teamcity")
        .get()
        .requiredVersion

    allowSnapshotVersions = true
    validateBeanDefinition = ValidationMode.FAIL
}

spotless {
    kotlin {
        ktlint()
    }
}
