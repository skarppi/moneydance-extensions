val privateKeyFile: String by rootProject
val publicKeyFile: String by rootProject
val privateKeyId: String by rootProject

val libDir = "$rootDir/lib"

val distFile = File("${projectDir}/${buildDir.name}/libs/${project.name}.jar")

task<JavaExec>("genKeys") {
    description = "Generates a passphrase-protected key pair."
    standardInput = System.`in`
    classpath = files("$libDir/extadmin.jar", "$libDir/moneydance-dev.jar")
    mainClass.set("com.moneydance.admin.KeyAdmin")
    args(listOf("genkey",  "$rootDir/$privateKeyFile", "$rootDir/$publicKeyFile"))
}

task<JavaExec>("sign") {
    dependsOn("installDist")

    description = "Signs an MXT file with a private key that must already exist"
    onlyIf {
        File(rootDir,  privateKeyFile).exists() && distFile.exists()
    }
    standardInput = System.`in`
    classpath = files("$libDir/extadmin.jar", "$libDir/moneydance-dev.jar")
    mainClass.set("com.moneydance.admin.KeyAdmin")
    args(listOf(
        "signextjar",
        "$rootDir/$privateKeyFile",
        privateKeyId,
        project.name,
        distFile
    ))
    doLast {
        println("Signed ${distFile}")
    }
}

task("deploy") {
    dependsOn("sign")

    doLast {
        val sourceFile = File("${projectDir}/s-${project.name}.mxt")

        val deployDir: String by rootProject
        val deployFile = File(deployDir.replaceFirst("~", System.getProperty("user.home")),
            "${project.name}.mxt")
        println("Copying $sourceFile to $deployFile")
        deployFile.delete()
        if (!sourceFile.renameTo(deployFile)) {
            println("Deploy failed!!!!!!!!!!!!!")
        }
    }
}

task("deployUnsigned") {
    dependsOn("installDist")

    doFirst {
        val debugDir: String by project
        val runFile = File(debugDir.replaceFirst("~", System.getProperty("user.home")),
            "${project.name}.mxt")
        println("Copying $distFile to $runFile")
        runFile.delete()
        if (!distFile.renameTo(runFile)) {
            println("Deploy failed!!!!!!!!!!!!!")
        }
    }
}
