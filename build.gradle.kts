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

tasks.test {
    useJUnitPlatform()
}

dependencies {
    compileOnly("net.minestom:minestom-snapshots:d0754f2a15")
    api("ch.qos.logback:logback-classic:1.5.7")
    api("net.kyori:adventure-text-minimessage:4.17.0")
    api("dev.hollowcube:polar:1.11.3")

    // gradle task
    compileOnly(gradleApi())

    // tests
    testImplementation("net.minestom:minestom-snapshots:d0754f2a15")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
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
