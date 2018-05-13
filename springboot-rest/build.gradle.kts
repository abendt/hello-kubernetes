import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStopContainer
import com.bmuschko.gradle.docker.tasks.container.extras.DockerWaitHealthyContainer
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import org.springframework.boot.gradle.tasks.bundling.BootJar

buildscript {
    repositories {
        jcenter()
    }
}

plugins {
    val kotlinVersion = "1.2.41"

    kotlin("jvm") version kotlinVersion
    id("org.springframework.boot") version "2.0.2.RELEASE"
    id("io.spring.dependency-management") version "1.0.5.RELEASE"
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("com.bmuschko.docker-remote-api") version "3.2.8"
}

repositories {
    jcenter()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))
    compile("org.springframework.boot:spring-boot-starter")
    compile("org.springframework.boot:spring-boot-starter-webflux")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.5")
    compile("org.jetbrains.kotlinx:kotlinx-html-jvm:0.6.10")

    testCompile("org.springframework.boot:spring-boot-starter-test")
    testCompile("io.projectreactor:reactor-test")
    testCompile(kotlin("test-junit"))
    testCompile("net.wuerl.kotlin:assertj-core-kotlin:0.2.1")
    testCompile("org.assertj:assertj-core:3.9.1")
    testCompile("com.nhaarman:mockito-kotlin-kt1.1:1.5.0")
    testCompile("io.rest-assured:rest-assured:3.1.0")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    val bootJar by getting(BootJar::class)

    val unpackFatJar by creating(Copy::class) {
        dependsOn(bootJar)
        val dockerWorkingDir = file("$buildDir/docker")

        val archivePath = bootJar.archivePath

        from(zipTree(archivePath))

        into(dockerWorkingDir)
        includeEmptyDirs = false

        // hack to preserve modification date - required for Docker cache
        val copyDetails = mutableListOf<FileCopyDetails>()

        eachFile { copyDetails.add(this) }
        doLast { restoreLastModifiedDate(copyDetails, dockerWorkingDir) }
    }

    val dockerBuildImage by creating(DockerBuildImage::class) {
        dependsOn(unpackFatJar)
        tag = "hellorest"
        inputDir = projectDir
    }

    val dockerCreateContainer by creating(DockerCreateContainer::class) {
        dependsOn(dockerBuildImage)
        targetImageId { dockerBuildImage.imageId }
        portBindings = listOf("8080:8080")
    }

    val dockerStartContainer by creating(DockerStartContainer::class) {
        dependsOn(dockerCreateContainer)
        targetContainerId { dockerCreateContainer.containerId }
    }

    val dockerWaitHealthy by creating(DockerWaitHealthyContainer::class) {
        dependsOn(dockerStartContainer)
        targetContainerId { dockerCreateContainer.containerId }
    }

    val dockerStopContainer by creating(DockerStopContainer::class) {
        targetContainerId { dockerCreateContainer.containerId }
    }

    val test by getting(Test::class) {
        include("**/*Test.*", "**/*IT.*")
    }

    val systemTest by creating(Test::class) {
        include("**/*ST.*")

        dependsOn(dockerStartContainer,dockerWaitHealthy)
        finalizedBy(dockerStopContainer)
    }

    "check" {
        dependsOn("systemTest")
    }
}

fun restoreLastModifiedDate(copyDetails: List<FileCopyDetails>, dir: File) {
    copyDetails.forEach { details ->
        val target = File(dir, details.path)
        if (target.exists()) {
            target.setLastModified(details.lastModified)
        }
    }
}
