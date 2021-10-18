import org.ajoberstar.grgit.Grgit

plugins {
    kotlin("jvm") version "1.5.10"
    application
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("org.ajoberstar.grgit") version "4.1.0"
}

group = "com.eddsteel"
version = Grgit.open()?.head()?.abbreviatedId ?: "0"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/cyberdelia/starlark")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(platform("io.ktor:ktor-bom:1.6.1"))

    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.11.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.1")
    implementation("com.lapanthere:starlark:4.2.1")
}

application {
    mainClass.set("com.eddsteel.feedfilter.MainKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveVersion.set("")
    }

    shadowDistZip {
        archiveBaseName.set("feedfilter")
        archiveVersion.set("")
        exclude("*.bat")
    }

    shadowDistTar {
        archiveBaseName.set("feedfilter")
        archiveVersion.set("")
        exclude("*.bat")
    }
}
