import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

buildscript {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
        if (project.hasProperty("nexusBaseUrl")) {
            maven {
                credentials {
                    username = project.property("nexusUserName").toString()
                    password = project.property("nexusPassword").toString()
                }
                url = uri("${project.property("nexusBaseUrl")}/repositories/releases")
            }
        }
        mavenLocal()
    }

    dependencies {
        classpath("com.xebialabs.gradle.plugins:gradle-xl-defaults-plugin:${properties["xlDefaultsPluginVersion"]}")
    }
}

plugins {
    kotlin("jvm") version "1.4.20"
    `kotlin-dsl-base`

    id("idea")
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("maven-publish")
    id("nebula.release") version "15.3.1"
    id("signing")
}

group = "com.xebialabs.gradle.plugins"
project.defaultTasks = listOf("build")

val releasedVersion = "2.0.0-${LocalDateTime.now().format(DateTimeFormatter.ofPattern("Mdd.Hmm"))}"
project.extra.set("releasedVersion", releasedVersion)

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
    if (project.hasProperty("nexusBaseUrl")) {
        maven {
            credentials {
                username = project.property("nexusUserName").toString()
                password = project.property("nexusPassword").toString()
            }
            url = uri("${project.property("nexusBaseUrl")}/repositories/releases")
        }
    }
}

idea {
    module {
        setDownloadJavadoc(true)
        setDownloadSources(true)
    }
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())

    implementation("org.apache.derby:derbynet:${properties["derbyVersion"]}")
    implementation("org.apache.derby:derbyclient:${properties["derbyVersion"]}")
    implementation("org.jetbrains.kotlin:kotlin-allopen:${properties["kotlin"]}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${properties["kotlin"]}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${properties["kotlin"]}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${properties["kotlin"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${properties["coroutinesVersion"]}")
}

if (project.hasProperty("sonatypeUsername") && project.hasProperty("public")) {
    publishing {
        publications {
            register("pluginMaven", MavenPublication::class) {
                from(components["java"])

                groupId = "com.xebialabs.gradle.plugins"
                artifactId = "gradle-xl-derby-plugin"

                pom {
                    name.set("Gradle Derby Plugin")
                    description.set("The easy way to get start/stop Derby with Gradle")
                    url.set("https://github.com/xebialabs/gradle-xl-derby-plugin.git")
                    licenses {
                        license {
                            name.set("GPLv2 with Digital.ai FLOSS License Exception")
                            url.set("https://github.com/xebialabs/gradle-xl-derby-plugin/blob/master/LICENSE")
                        }
                    }

                    scm {
                        url.set("https://github.com/xebialabs/gradle-xl-derby-plugin")
                    }

                    developers {
                        developer {
                            id.set("mwinkels")
                            name.set("Maarten Winkels")
                            email.set("mwinkels@digital.ai")
                        }
                        developer {
                            id.set("vpugar-digital")
                            name.set("Vedran Pugar")
                            email.set("vpugar@digital.ai")
                        }
                        developer {
                            id.set("bnechyporenko")
                            name.set("Bogdan Nechyporenko")
                            email.set("bnechyporenko@digital.ai")
                        }
                    }
                }
                versionMapping {
                    usage("java-api") {
                        fromResolutionOf("runtimeClasspath")
                    }
                    usage("java-runtime") {
                        fromResolutionResult()
                    }
                }
            }
        }
        repositories {
            maven {
                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                credentials {
                    username = project.property("sonatypeUsername").toString()
                    password = project.property("sonatypePassword").toString()
                }
            }
            maven {
                url = uri("https://oss.sonatype.org/content/repositories/snapshots")
                credentials {
                    username = project.property("sonatypeUsername").toString()
                    password = project.property("sonatypePassword").toString()
                }
            }
        }
    }

    signing {
        sign(publishing.publications["pluginMaven"])
    }

    nexusPublishing {
        repositories {
            sonatype {
                username.set(project.property("sonatypeUsername").toString())
                password.set(project.property("sonatypePassword").toString())
            }
        }
    }
} else {
    publishing {
        publications {
            register("pluginMaven", MavenPublication::class) {
                from(components["java"])
            }
        }
        repositories {
            maven {
                url = uri("${project.property("nexusBaseUrl")}/repositories/releases")
                credentials {
                    username = project.property("nexusUserName").toString()
                    password = project.property("nexusPassword").toString()
                }
            }
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
    withJavadocJar()
}

tasks {
    register<NebulaRelease>("nebulaRelease")

    named<Test>("test") {
        useJUnitPlatform()
    }

    compileKotlin {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    }

    withType<ValidatePlugins>().configureEach {
        failOnWarning.set(false)
        enableStricterValidation.set(false)
    }
}
