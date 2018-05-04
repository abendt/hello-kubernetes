import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.bmuschko.gradle.docker.DockerRegistryCredentials

buildscript {
    repositories {
        jcenter()
    }
}

plugins {
    val kotlinVersion = "1.2.31"

    application
    kotlin("jvm") version kotlinVersion
    id("org.springframework.boot") version "2.0.0.RELEASE"
    id("io.spring.dependency-management") version "1.0.4.RELEASE"
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("com.bmuschko.docker-java-application") version "3.2.5"
}

repositories {
    jcenter()
}

application {
    mainClassName = "hellorest.ApplicationKt"
}

docker {
    javaApplication {
        baseImage = "anapsix/alpine-java:8_server-jre"
        setPorts(setOf(8080))
        tag = "hellorest"
    }

  
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))
    compile("org.springframework.boot:spring-boot-starter")
    compile("org.springframework.boot:spring-boot-starter-webflux")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.5")
    compile("org.jetbrains.kotlinx:kotlinx-html-jvm:0.6.9")

    testCompile("org.springframework.boot:spring-boot-starter-test")
    testCompile("io.projectreactor:reactor-test")
    testCompile(kotlin("test-junit"))
    testCompile("net.wuerl.kotlin:assertj-core-kotlin:0.2.1")
    testCompile("org.assertj:assertj-core:3.9.1")
    testCompile("com.nhaarman:mockito-kotlin-kt1.1:1.5.0")
    testCompile("io.rest-assured:rest-assured:3.0.7")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    "dockerBuildImage" {
      dependsOn("bootJar")
    }
}