plugins {
    id("java")
}

group = "org.krystilize"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("net.minestom:minestom-snapshots:6c5cd6544e")
}
