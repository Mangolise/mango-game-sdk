plugins {
    id("java")
    id("maven-publish")
}

group = "net.mangolise"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("net.minestom:minestom-snapshots:6c5cd6544e")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "net.mangolise.gamesdk"
            artifactId = "library"
            version = "1.1"

            from(components["java"])
        }
    }
}
