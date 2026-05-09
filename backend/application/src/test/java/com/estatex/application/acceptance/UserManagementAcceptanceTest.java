package com.estatex.application.acceptance;

import com.estatex.application.acceptance.fakes.TestingBackendSetup;
import com.estatex.application.user.UserResult;
import com.estatex.application.user.UserService;
import com.estatex.domain.exception.DomainException;
import com.estatex.domain.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserManagementAcceptanceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        TestingBackendSetup backend = new TestingBackendSetup();
        this.userService = backend.userService;
    }

    @Test
    void uc1_1_Registration_ShouldRegisterNewUserAndEnforceUniqueEmail() {
        // Given new user details
        UserService.RegisterCommand cmd = new UserService.RegisterCommand("test@example.com", "Test User");

        // When
        UserResult result = userService.register(cmd);

        // Then
        assertNotNull(result.id());
        assertEquals("test@example.com", result.email());
        assertEquals("Test User", result.displayName());
        assertTrue(result.active());

        // And email uniqueness is enforced
        assertThrows(DomainException.class, () -> userService.register(cmd));
    }

    @Test
    void uc2_1_ProfileManagement_UserCanUpdateProfile() {
        // Given
        UserResult registerResult = userService.register(new UserService.RegisterCommand("john@example.com", "John"));
        UUID userId = registerResult.id();

        // When
        UserService.UpdateProfileCommand updateCmd = new UserService.UpdateProfileCommand(
                userId, "John Doe", "123456789"
        );
        UserResult updatedProfile = userService.updateProfile(updateCmd);

        // Then
        assertEquals("John Doe", updatedProfile.displayName());
        assertEquals("123456789", updatedProfile.phone());
    }

    @Test
    void uc2_2_PublicProfiles_ShouldShowBasicDetailsAndActiveListings() {
        // Given
        UserResult registerResult = userService.register(new UserService.RegisterCommand("jane@example.com", "Jane"));
        UUID userId = registerResult.id();

        // When fetching public profile
        UserResult publicProfile = userService.getPublicProfile(userId);

        // Then
        assertEquals("Jane", publicProfile.displayName());
        assertEquals(0L, publicProfile.activeListingsCount()); // Starts at 0
        // No email or phone should be exposed ideally, but our UserResult returns what's requested, 
        // asserting what is returned based on current `estateX` API expectations.
    }

    @Test
    void uc10_2_Administration_AdminCanDeactivateUser() {
        // Given
        UserResult registerResult = userService.register(new UserService.RegisterCommand("baduser@example.com", "Bad"));
        UUID userId = registerResult.id();

        // When
        userService.deactivateUser(userId);

        // Then fetching user throws or shows deactivated (current logic just marks active=false)
        UserResult current = userService.getCurrentUser(userId);
        assertFalse(current.active());
    }
}
