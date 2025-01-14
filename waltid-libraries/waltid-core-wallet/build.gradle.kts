plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("maven-publish")
}

group = "id.walt.wallet"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        withJava()
    }

    jvmToolchain(21)

    sourceSets {
        val ktor_version = "2.3.12"

        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

                implementation("io.github.oshai:kotlin-logging-jvm:7.0.0")

                implementation(project(":waltid-libraries:crypto:waltid-crypto"))
                implementation(project(":waltid-libraries:waltid-did"))

                implementation(project(":waltid-libraries:protocols:waltid-openid4vc"))
                implementation(project(":waltid-libraries:sdjwt:waltid-sdjwt"))
                implementation(project(":waltid-libraries:credentials:waltid-mdoc-credentials"))
                implementation(project(":waltid-libraries:credentials:waltid-verifiable-credentials"))

                // Ktor client
                implementation("io.ktor:ktor-client-core:$ktor_version")
                implementation("io.ktor:ktor-client-serialization:$ktor_version")
                implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
                implementation("io.ktor:ktor-client-json:$ktor_version")
                implementation("io.ktor:ktor-client-logging:$ktor_version")

                // Bouncy Castle
                implementation("org.bouncycastle:bcprov-lts8on:2.73.6")
                implementation("org.bouncycastle:bcpkix-lts8on:2.73.6")

                // Problematic libraries:
                implementation("com.nimbusds:nimbus-jose-jwt:9.41.1")
                implementation("com.augustcellars.cose:cose-java:1.1.0")
            }
        }
        val jvmMain by getting {
            dependencies {
                // Ktor client
                implementation("io.ktor:ktor-client-okhttp-jvm:$ktor_version")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
                implementation("org.slf4j:slf4j-simple:2.0.16")
            }
        }
    }
}

publishing {
    repositories {
        maven {
            val releasesRepoUrl = uri("https://maven.waltid.dev/releases")
            val snapshotsRepoUrl = uri("https://maven.waltid.dev/snapshots")
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            val envUsername = System.getenv("MAVEN_USERNAME")
            val envPassword = System.getenv("MAVEN_PASSWORD")

            val usernameFile = File("$rootDir/secret_maven_username.txt")
            val passwordFile = File("$rootDir/secret_maven_password.txt")

            val secretMavenUsername = envUsername ?: usernameFile.let { if (it.isFile) it.readLines().first() else "" }
            //println("Deploy username length: ${secretMavenUsername.length}")
            val secretMavenPassword = envPassword ?: passwordFile.let { if (it.isFile) it.readLines().first() else "" }

            //if (secretMavenPassword.isBlank()) {
            //   println("WARNING: Password is blank!")
            //}

            credentials {
                username = secretMavenUsername
                password = secretMavenPassword
            }
        }
    }
}
