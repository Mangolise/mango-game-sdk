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
    implementation("ch.qos.logback:logback-classic:1.5.7")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "com.github.Mangolise"
            artifactId = "mango-game-sdk"
            version = "main-SNAPSHOT"

            from(components["java"])
        }
    }
}
