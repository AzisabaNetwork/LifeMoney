/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    `java-library`
    `maven-publish`
    id("io.github.goooler.shadow") version "8.1.8"
}

repositories {
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.maven.apache.org/maven2/")
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://jitpack.io" )
}

dependencies {
    compileOnly(libs.io.papermc.paper.paper.api)
    compileOnly("io.lumine:Mythic-Dist:5.7.2")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    implementation("com.zaxxer:HikariCP:6.0.0")
}

group = "net.azisaba"
version = "1.2.0"
description = "LifeMoney"
java.sourceCompatibility = JavaVersion.VERSION_21

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks {
    withType<JavaCompile> { options.encoding = "UTF-8" }
    withType<Javadoc> { options.encoding = "UTF-8" }

    shadowJar {
        relocate("org.jetbrains", "net.azisaba.lifemoney.lib.org.jetbrains")
        relocate("com.zaxxer.hikari", "net.azisaba.lifemoney.lib.com.zaxxer")
    }
}
