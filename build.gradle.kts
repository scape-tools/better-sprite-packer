plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.70"
    id("org.openjfx.javafxplugin") version "0.0.8"
    application
}

javafx {
    modules("javafx.controls", "javafx.fxml", "javafx.swing")
}

repositories {
    jcenter()
    maven {
        setUrl("https://plugins.gradle.org/m2/")
    }
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.apache.commons", "commons-imaging", "1.0-alpha1")
}

application {
    mainClassName = "io.nshusa.App"
}
