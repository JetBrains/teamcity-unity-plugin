plugins {
    kotlin("jvm")
}

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
