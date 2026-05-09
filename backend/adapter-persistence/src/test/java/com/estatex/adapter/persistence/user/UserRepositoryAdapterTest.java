package com.estatex.adapter.persistence.user;

import com.estatex.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryAdapterTest {

    @Autowired
    private UserJpaRepository jpaRepository;

    private UserRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UserRepositoryAdapter(jpaRepository);
    }

    @Test
    void shouldSaveAndFindUser() {
        User user = User.create("john@example.com", "John Doe");
        User saved = adapter.save(user);

        assertNotNull(saved.getId());

        Optional<User> found = adapter.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("john@example.com", found.get().getEmail());
        assertEquals("John Doe", found.get().getDisplayName());
        assertTrue(found.get().isActive());
    }

    @Test
    void shouldFindUserByEmail() {
        User user = User.create("jane@example.com", "Jane Doe");
        adapter.save(user);

        Optional<User> found = adapter.findByEmail("jane@example.com");
        assertTrue(found.isPresent());
        assertEquals("Jane Doe", found.get().getDisplayName());

        assertFalse(adapter.findByEmail("unknown@example.com").isPresent());
    }

    @Test
    void shouldCheckIfExistsByEmail() {
        User user = User.create("exists@example.com", "Exists User");
        adapter.save(user);

        assertTrue(adapter.existsByEmail("exists@example.com"));
        assertFalse(adapter.existsByEmail("doesnotexist@example.com"));
    }

    @Test
    void shouldThrowWhenEmailNotUnique() {
        adapter.save(User.create("unique@example.com", "User 1"));
        
        assertThrows(DataIntegrityViolationException.class, () -> {
            adapter.save(User.create("unique@example.com", "User 2"));
            jpaRepository.flush(); // Flush required to trigger DB constraint immediately in Hibernate
        });
    }
}
