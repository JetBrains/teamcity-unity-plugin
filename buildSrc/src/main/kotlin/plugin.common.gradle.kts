plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks {
    test {
        useTestNG()
    }

    compileKotlin {
        compilerOptions {
            allWarningsAsErrors.set(true)
        }
    }
}
