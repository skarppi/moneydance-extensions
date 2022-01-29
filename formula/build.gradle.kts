plugins {
    application
    id("common.java-conventions")
}

dependencies {
    implementation(project(":common"))
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("MainKt")
}

tasks.jar {
    dependsOn(project(":common").tasks.jar)

    from(configurations.runtimeClasspath.get()
        .filter { it.name.contains("common.jar") }
        .onEach { println("add from dependencies: ${it.name}") }
        .map { zipTree(it) }
    )
}