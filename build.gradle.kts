plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.23"
    id("org.jetbrains.intellij") version "1.17.3"
}

group = "com.jgayb"
version = "0.3.9"

repositories {
    maven { url = uri("https://www.jetbrains.com/intellij-repository/releases") }
    maven { url = uri("https://maven.aliyun.com/nexus/content/groups/public/") }
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2023.2.6")
    type.set("IU") // Target IDE Platform

    pluginName.set("fenix")
    plugins.set(
        listOf(
            "java",
            "Kotlin",
            "Spring"
        )
    )
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("242.*")
        changeNotes.set(
            """
If you have any comments please let me know.<br>
<em>
<p>0.3.9:</p>
<ul>
  <li>Kotlin K2</li>
</ul>
</em>
<em>
<p>0.3.8:</p>
<ul>
  <li>Fix link icon display bug</li>
</ul>
</em>
<em>
<p>0.3.7:</p>
<ul>
  <li>Fix link icon display bug</li>
</ul>
</em>
<em>
<p>0.3.6:</p>
<ul>
  <li></li>
</ul>
</em>
<em>
<p>0.3.5:</p>
<ul>
  <li>Fix bugs</li>
</ul>
</em>
<em>
<p>0.3.4:</p>
<ul>
  <li>New features for kotlin</li>
</ul>
</em>
<em>
<p>0.3.3:</p>
<ul>
  <li>fix bugs</li>
</ul>
</em>
"""
        )
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
