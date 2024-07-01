plugins {
    antlr
    id("idea")
    id("plugin.common")
}

repositories {
    mavenCentral()
}

dependencies {
    antlr(libs.antlr)
}

// generate java from the ANTLR g4 files
val generatedSrc = file("./build/generated-src/antlr/main")

tasks.generateGrammarSource {
    arguments = listOf("-visitor", "-package", "org.jetbrains.unity", "-Xexact-output-dir")
    outputDirectory = generatedSrc
}

// mark the generated source as such for IntelliJ IDEA
sourceSets {
    main {
        java {
            srcDirs(generatedSrc)
        }
    }
}

idea {
    module {
        generatedSourceDirs.add(generatedSrc)
    }
}

tasks.compileKotlin {
    dependsOn(tasks.generateGrammarSource)
}