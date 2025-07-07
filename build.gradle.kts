plugins {
    id("java")
    id("java-library")
    id("maven-publish")
}

var versionStr = System.getenv("GIT_COMMIT") ?: "dev"

group = "net.mangolise"
version = versionStr

repositories {
    mavenLocal()
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}

// use unicode
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

dependencies {
    compileOnly("net.minestom:minestom:2025.07.03-1.21.5")
    api("ch.qos.logback:logback-classic:1.5.13")
    api("net.kyori:adventure-text-minimessage:4.17.0")
    api("dev.hollowcube:polar:1.14.5")

    // gradle task
    compileOnly(gradleApi())

    // tests
    testImplementation("net.minestom:minestom:2025.07.03-1.21.5")
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
