import org.gradle.jvm.tasks.Jar

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.70"
    id("org.openjfx.javafxplugin") version "0.0.8"
    application
}

javafx {
    version = "11.0.2"
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
    runtimeOnly("org.openjfx:javafx-controls:${javafx.version}:mac")
    runtimeOnly("org.openjfx:javafx-fxml:${javafx.version}:mac")
    runtimeOnly("org.openjfx:javafx-swing:${javafx.version}:mac")

    runtimeOnly("org.openjfx", "javafx-controls", "${javafx.version}:linux")
    runtimeOnly("org.openjfx", "javafx-fxml", "${javafx.version}:linux")
    runtimeOnly("org.openjfx", "javafx-swing", "${javafx.version}:linux")

    runtimeOnly("org.openjfx", "javafx-controls", "${javafx.version}:win")
    runtimeOnly("org.openjfx", "javafx-fxml", "${javafx.version}:win")
    runtimeOnly("org.openjfx", "javafx-swing", "${javafx.version}:win")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.apache.commons", "commons-imaging", "1.0-alpha1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

application {
    mainClassName = "io.nshusa.App"
}

val fatJar = task("fatJar", type = Jar::class) {
    baseName = "bsp4-gui.jar"
    manifest {
        attributes["Main-Class"] = application.mainClassName
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
