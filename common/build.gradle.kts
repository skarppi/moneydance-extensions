plugins {
    `java-library`
}

val moneydanceLibs: String by rootProject

dependencies {
    api("org.apache.commons:commons-lang3:3.12.0")
    api(
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
}