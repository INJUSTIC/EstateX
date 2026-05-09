plugins {
    id("io.spring.dependency-management")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.4")
    }
}

dependencies {
    implementation(project(":domain"))

    // Spring annotations only (no web/data) – acceptable in application layer
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-tx")

    // Validation API (JSR-380) for command validation
    implementation("jakarta.validation:jakarta.validation-api:3.1.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
