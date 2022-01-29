import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL
import java.io.FileOutputStream

plugins {
    kotlin("jvm") version "1.5.10"
    application
}

allprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }

    repositories {
        mavenCentral()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    tasks.test {
        useTestNG()
    }
}


val moneydanceLibs: String by project

dependencies {
    implementation(fileTree(moneydanceLibs) {
        include("*.jar")
    })
    testImplementation(kotlin("test"))
}

val devKitFile = File("moneydance-devkit-5.1.tar.gz")

task("downloadDevkit") {
    doFirst {
        URL("https://infinitekind.com/dev/moneydance-devkit-5.1.tar.gz").openStream().use { input ->
            FileOutputStream(devKitFile).use { output ->
                input.copyTo(output)
            }
        }
    }
}

task<Copy>("fetchLibs") {
    dependsOn("downloadDevkit")

    from(tarTree(devKitFile))
    {
        include("moneydance-devkit-5.1/lib/**")
        eachFile {
            relativePath = RelativePath(true, *relativePath.segments.drop(2).toTypedArray())
        }
    }
    into("lib")

    doLast {
        devKitFile.delete()
    }
}

application {
    mainClass.set("Moneydance")
    description = "Run full Moneydance GUI"
}