plugins {
    application
    id("common.java-conventions")
}

dependencies {
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.2.0")
    implementation(project(":common"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useTestNG()
}

application {
    mainClass.set("MainKt")
}