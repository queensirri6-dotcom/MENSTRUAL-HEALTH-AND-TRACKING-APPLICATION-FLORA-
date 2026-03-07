plugins {
    kotlin("jvm") version "1.9.23"
    application
}

group = "com.gradingcalculator"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")
    implementation("org.apache.xmlbeans:xmlbeans:5.2.0")
    implementation("org.apache.commons:commons-compress:1.26.1")
    implementation("commons-io:commons-io:2.15.1")
    implementation(kotlin("stdlib"))
}

application {
    mainClass.set("MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })
}

kotlin {
    jvmToolchain(17)
}
