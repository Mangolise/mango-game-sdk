plugins {
    id("java")
    id("java-library")
    id("maven-publish")
}

var versionStr = System.getenv("GIT_COMMIT") ?: "dev"

group = "net.mangolise"
version = versionStr

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("net.minestom:minestom-snapshots:6c5cd6544e")
    api("ch.qos.logback:logback-classic:1.5.7")
    api("net.kyori:adventure-text-minimessage:4.17.0")
    api("dev.hollowcube:polar:1.11.1")

    // gradle task
    compileOnly(gradleApi())

    // tests
    testImplementation("net.minestom:minestom-snapshots:6c5cd6544e")
}

java {
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            name = "serbleMaven"
            url = uri("https://maven.serble.net/snapshots/")
            credentials {
                username = System.getenv("SERBLE_REPO_USERNAME")?:""
                password = System.getenv("SERBLE_REPO_PASSWORD")?:""
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("mavenGitCommit") {
            groupId = "net.mangolise"
            artifactId = "mango-game-sdk"
            version = versionStr
            from(components["java"])
        }

        create<MavenPublication>("mavenLatest") {
            groupId = "net.mangolise"
            artifactId = "mango-game-sdk"
            version = "latest"
            from(components["java"])
        }
    }
}
