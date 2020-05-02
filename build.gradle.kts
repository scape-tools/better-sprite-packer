import org.gradle.jvm.tasks.Jar

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.70"
    id("org.openjfx.javafxplugin") version "0.0.8"
    id("com.github.johnrengelman.shadow") version "5.1.0"
    application
}

javafx {
    version = "14"
    modules("javafx.controls", "javafx.fxml", "javafx.swing")
}

repositories {
    mavenCentral()
    jcenter()
    maven {
        setUrl("https://plugins.gradle.org/m2/")
    }
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.apache.commons", "commons-imaging", "1.0-alpha1")
    runtimeOnly("org.openjfx", "javafx-controls", "${javafx.version}:mac")
    runtimeOnly("org.openjfx", "javafx-fxml", "${javafx.version}:mac")
    runtimeOnly("org.openjfx", "javafx-swing", "${javafx.version}:mac")
}

var mainClassNamePath = "io.nshusa.App"

application {
    mainClassName = mainClassNamePath
}

val fatJar = task("fatJar", type = Jar::class) {
    baseName = "bsp4-gui.jar"
    manifest {
        attributes["Main-Class"] = mainClassNamePath
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}
