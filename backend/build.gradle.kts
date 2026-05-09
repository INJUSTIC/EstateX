import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    java
    id("org.springframework.boot") version "3.3.4" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
    id("info.solidsoft.pitest") version "1.15.0" apply false
}

extra["springdocVersion"] = "2.3.0"
extra["lombokVersion"] = "1.18.30"
extra["junitVersion"] = "5.10.1"
extra["mockitoVersion"] = "5.8.0"
extra["pitestJunit5Version"] = "1.2.1"
extra["mapstructVersion"] = "1.5.5.Final"

subprojects {
    repositories {
        mavenCentral()
    }
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "jacoco")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
    }

    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.4")
        }
    }

    dependencies {
        // Lombok
        val lombokVersion = rootProject.extra["lombokVersion"] as String
        compileOnly("org.projectlombok:lombok:$lombokVersion")
        annotationProcessor("org.projectlombok:lombok:$lombokVersion")

        // Testing
        val junitVersion = rootProject.extra["junitVersion"] as String
        val mockitoVersion = rootProject.extra["mockitoVersion"] as String
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
        testImplementation("org.mockito:mockito-core:$mockitoVersion")
        testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    // ── JaCoCo ────────────────────────────────────────────────────────────────
    tasks.named<JacocoReport>("jacocoTestReport") {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
        violationRules {
            rule {
                limit {
                    minimum = "1.00".toBigDecimal()
                }
            }
        }
    }

    tasks.test {
        finalizedBy(tasks.named("jacocoTestReport"))
    }
}

// ── PiTest – only on domain and application modules ───────────────────────────
listOf(":domain", ":application").forEach { modulePath ->
    project(modulePath) {
        apply(plugin = "info.solidsoft.pitest")

        configure<info.solidsoft.gradle.pitest.PitestPluginExtension> {
            val pitestJunit5Version = rootProject.extra["pitestJunit5Version"] as String
            junit5PluginVersion.set(pitestJunit5Version)
            targetClasses.set(setOf("com.estatex.*"))
            targetTests.set(setOf("com.estatex.*"))
            mutationThreshold.set(100)
            testStrengthThreshold.set(100)
            outputFormats.set(setOf("HTML", "XML"))
            timestampedReports.set(false)
            threads.set(4)
        }
    }
}
