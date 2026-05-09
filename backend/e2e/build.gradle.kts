plugins {
    id("io.spring.dependency-management")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.4")
    }
}

configurations.all {
    // Pin Testcontainers to 1.21.3; Spring Boot 3.3.x BOM pulls in 1.19.x.
    resolutionStrategy.eachDependency {
        if (requested.group == "org.testcontainers") {
            useVersion("1.21.3")
            because("Consistent Testcontainers version across all modules")
        }
    }
}

dependencies {
    testImplementation(project(":adapter-web"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.apache.httpcomponents.client5:httpclient5")
    testRuntimeOnly("org.postgresql:postgresql")
}

tasks.withType<Test> {
    environment("DOCKER_HOST", "unix:///var/run/docker.sock")
    systemProperty("api.version", "1.41")
}
