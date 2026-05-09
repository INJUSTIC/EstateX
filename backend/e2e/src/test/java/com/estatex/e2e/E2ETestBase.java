package com.estatex.e2e;

import com.estatex.adapter.web.EstateXApplication;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

@SpringBootTest(classes = EstateXApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
public abstract class E2ETestBase {

    // JVM-wide singleton: started once, shared across all test classes,
    // so the cached Spring context always connects to the same container.
    static final PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer<>("postgres:16-alpine");
        postgres.start();
    }

    @DynamicPropertySource
    static void overrideDataSourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    protected TestRestTemplate http;

    @Autowired
    private void enablePatchSupport(TestRestTemplate testRestTemplate) {
        // HttpURLConnection does not support PATCH; replace with Apache HttpClient
        testRestTemplate.getRestTemplate()
                .setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void cleanDatabase() throws SQLException {
        //given: fresh state for each test
        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute(
                "TRUNCATE TABLE messages, conversations, favourites, photos, listings, users RESTART IDENTITY CASCADE"
            );
        }
    }

    protected UUID registerUser(String email, String name) {
        var response = http.postForObject("/api/auth/register",
                Map.of("email", email, "displayName", name), Map.class);
        return UUID.fromString((String) response.get("id"));
    }

    protected UUID createListing(UUID ownerId) {
        return createListing(ownerId, "Test Apartment", "Krakow", "APARTMENT", "RENT", 1500);
    }

    @SuppressWarnings("unchecked")
    protected UUID createListing(UUID ownerId, String title, String city,
                                 String propertyType, String transactionType, int price) {
        var body = Map.of(
                "title", title,
                "city", city,
                "country", "Poland",
                "propertyType", propertyType,
                "transactionType", transactionType,
                "price", price,
                "areaSqMeters", 50.0,
                "numberOfRooms", 2
        );
        var response = http.exchange("/api/listings", HttpMethod.POST,
                new HttpEntity<>(body, userHeaders(ownerId)), Map.class);
        return UUID.fromString((String) response.getBody().get("id"));
    }

    protected HttpHeaders userHeaders(UUID userId) {
        var headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
