import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
    application
}

group = "me.user"
version = "2.2"

val privateKeyFile: String by project
val publicKeyFile: String by project
val privateKeyId: String by project

val moneydanceLibs: String by project

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation(
        fileTree(moneydanceLibs) {
            // Minimal set of libraries required to compile and run the mock.
            // This can change between Moneydance versions.
            include(
                "dropbox-core-sdk-*.jar",
                "flatlaf-*.jar",
                "mdpython.jar",
                "moneydance.jar", // we are using some private APIs
            )
        }
    )
    testImplementation(kotlin("test"))
}

tasks.test {
    useTestNG()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

task<JavaExec>("genKeys") {
    description = "Generates a passphrase-protected key pair."
    standardInput = System.`in`
    classpath = files("lib/extadmin.jar", "lib/moneydance-dev.jar")
    mainClass.set("com.moneydance.admin.KeyAdmin")
    args(listOf("genkey",  privateKeyFile, publicKeyFile))
}

task<JavaExec>("sign") {
    description = "Signs an MXT file with a private key that must already exist"
    onlyIf {
        File(privateKeyFile as String).exists()
    }
    standardInput = System.`in`
    classpath = files("lib/extadmin.jar", "lib/moneydance-dev.jar")
    mainClass.set("com.moneydance.admin.KeyAdmin")
    args(listOf(
        "signextjar",
        privateKeyFile,
        privateKeyId,
        rootProject.name,
        File("${buildDir.name}/libs/${rootProject.name}-$version.jar")
    ))
}

task("deploy") {
    dependsOn("sign")

    doLast {
        val deployDir: String by project

        val deployFile = File(deployDir.replaceFirst("~", System.getProperty("user.home")),
            "${rootProject.name}.mxt")
        println("Copying to $deployFile")
        File("s-${rootProject.name}.mxt")
            .renameTo(deployFile)
    }
}

task<JavaExec>("runFull") {
    dependsOn("installDist")

    doFirst {
        val runDir: String by project
        val runFile = File(runDir.replaceFirst("~", System.getProperty("user.home")),
            "${rootProject.name}.mxt")
        println("Copying to $runFile")
        File("${buildDir.name}/libs/${rootProject.name}-$version.jar")
            .renameTo(runFile)
    }

    description = "Run full Moneydance GUI"
    classpath = fileTree(moneydanceLibs) {
        include("*.jar")
    }
    mainClass.set("Moneydance")
}

application {
    mainClass.set("MainKt")
}