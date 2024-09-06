# Game SDK

The Mango game SDK is a library that allows for easily creating minigames which can be easily integrated into
another project. Even if you want it to run as a standalone application if provides many helper methods and 
features system.

## Usage

### Dependency

build.gradle.kts
```kotlin
repositories {
    mavenCentral()
    maven("https://maven.serble.net/snapshots/")
}

dependencies {
    implementation("net.mangolise:mango-game-sdk:latest")
}
```

build.gradle
```groovy
repositories {
    maven { url 'https://maven.serble.net/snapshots/' }
}

dependencies {
    implementation 'net.mangolise:mango-game-sdk:latest'
}
```

pom.xml
```xml
<repositories>
    <repository>
        <id>Serble</id>
        <url>https://maven.serble.net/snapshots/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>net.mangolise</groupId>
        <artifactId>mango-game-sdk</artifactId>
        <version>latest</version>
    </dependency>
</dependencies>
```
