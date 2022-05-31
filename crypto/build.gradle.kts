plugins {
    application
    id("common.java-conventions")
}

dependencies {
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.2.0")
    implementation(project(":common"))
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("MainKt")
}

tasks.jar {
    dependsOn(project(":common").tasks.jar)

    from(configurations.runtimeClasspath.get()
        .filter { it.name.contains("common.jar")
                || it.name.contains("csv")
                || it.name.contains("kotlin-logging")
                || it.name.contains("slf4j") }
        .onEach { println("add from dependencies: ${it.name}") }
        .map { zipTree(it) }
    )
}