#!/usr/bin/env kotlin



import kotlin.system.exitProcess
import kotlin.text.*

val tag = args.firstOrNull()

fun exit(message: String?) {
    if (message != null) {
        println(message)
    }
    exitProcess(1)
}

if (tag.isNullOrEmpty()) {
    exit("tag is missing, aborting")
}

if (tag?.startsWith("tags/v") == false) {
    exit("release version must start with 'v'")
}

val version = tag?.substring("tags/v".length)

// https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string
val semverRegex = """
    ^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?${'$'}
""".trimIndent().toRegex()

if (!semverRegex.matches(version.orEmpty())) {
    exit("specified version seems not to be a valid semver: $version")
}

println("release version: $version")
println("##teamcity[setParameter name='env.ORG_GRADLE_PROJECT_version' value='${version}']")