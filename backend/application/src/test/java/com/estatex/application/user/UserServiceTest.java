package com.estatex.application.user;

import com.estatex.application.port.out.FileStoragePort;
import com.estatex.domain.exception.DomainException;
import com.estatex.domain.exception.UserNotFoundException;
import com.estatex.domain.user.User;
import com.estatex.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private UserService userService;

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    void shouldRegisterUserWhenEmailNotTaken() {
        //given
        var cmd = new UserService.RegisterCommand("jan@example.com", "Jan Kowalski");
        when(userRepository.existsByEmail("jan@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        //when
        UserResult result = userService.register(cmd);

        //then
        assertEquals("jan@example.com", result.email());
    }

    @Test
    void shouldSaveUserWhenRegistrationSucceeds() {
        //given
        var cmd = new UserService.RegisterCommand("jan@example.com", "Jan");
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        //when
        userService.register(cmd);

        //then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldThrowWhenEmailAlreadyRegistered() {
        //given
        var cmd = new UserService.RegisterCommand("jan@example.com", "Jan");
        when(userRepository.existsByEmail("jan@example.com")).thenReturn(true);

        //when / then
        assertThrows(DomainException.class, () -> userService.register(cmd));
    }

    @Test
    void shouldNotSaveUserWhenEmailAlreadyRegistered() {
        //given
        var cmd = new UserService.RegisterCommand("jan@example.com", "Jan");
        when(userRepository.existsByEmail("jan@example.com")).thenReturn(true);

        //when
        try { userService.register(cmd); } catch (DomainException ignored) {}

        //then
        verify(userRepository, never()).save(any());
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void shouldLoginUserWhenEmailExists() {
        //given
        UUID userId = UUID.randomUUID();
        var user = User.create("jan@example.com", "Jan");
        when(userRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(user));
        when(userRepository.countActiveListings(any())).thenReturn(0L);

        //when
        UserResult result = userService.login("jan@example.com");

        //then
        assertEquals("jan@example.com", result.email());
        assertEquals("Jan", result.displayName());
    }

    @Test
    void shouldThrowWhenLoginEmailNotFound() {
        //given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        //when / then
        assertThrows(DomainException.class, () -> userService.login("nonexistent@example.com"));
    }

    @Test
    void shouldIncludeActiveListingsCountOnLogin() {
        //given
        var user = User.create("jan@example.com", "Jan");
        when(userRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(user));
        when(userRepository.countActiveListings(any())).thenReturn(2L);

        //when
        UserResult result = userService.login("jan@example.com");

        //then
        assertEquals(2L, result.activeListingsCount());
    }

    // ── updateProfile ─────────────────────────────────────────────────────────

    @Test
    void shouldUpdateProfileWhenUserExists() {
        //given
        UUID userId = UUID.randomUUID();
        var user = User.create("jan@example.com", "Jan");
        var cmd = new UserService.UpdateProfileCommand(userId, "Jan Kowalski", "+48123456789");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.countActiveListings(any())).thenReturn(2L);

        //when
        UserResult result = userService.updateProfile(cmd);

        //then
        assertEquals("Jan Kowalski", result.displayName());
    }

    @Test
    void shouldThrowWhenUpdatingProfileForNonExistentUser() {
        //given
        UUID userId = UUID.randomUUID();
        var cmd = new UserService.UpdateProfileCommand(userId, "Jan", null);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        //when / then
        assertThrows(UserNotFoundException.class, () -> userService.updateProfile(cmd));
    }

    @Test
    void shouldIncludeActiveListingsCountInProfileResult() {
        //given
        UUID userId = UUID.randomUUID();
        var user = User.create("jan@example.com", "Jan");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.countActiveListings(any())).thenReturn(3L);

        //when
        UserResult result = userService.updateProfile(
                new UserService.UpdateProfileCommand(userId, "Jan", null));

        //then
        assertEquals(3L, result.activeListingsCount());
    }

    // ── getCurrentUser ────────────────────────────────────────────────────────

    @Test
    void shouldReturnUserWhenCurrentUserExists() {
        //given
        UUID userId = UUID.randomUUID();
        var user = User.create("jan@example.com", "Jan");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.countActiveListings(any())).thenReturn(0L);

        //when
        UserResult result = userService.getCurrentUser(userId);

        //then
        assertEquals("jan@example.com", result.email());
    }

    @Test
    void shouldThrowWhenCurrentUserNotFound() {
        //given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        //when / then
        assertThrows(UserNotFoundException.class, () -> userService.getCurrentUser(userId));
    }

    // ── getPublicProfile ──────────────────────────────────────────────────────

    @Test
    void shouldReturnPublicProfileWhenUserFound() {
        //given
        UUID userId = UUID.randomUUID();
        var user = User.create("jan@example.com", "Jan");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.countActiveListings(userId)).thenReturn(5L);

        //when
        UserResult result = userService.getPublicProfile(userId);

        //then
        assertEquals(5L, result.activeListingsCount());
    }

    @Test
    void shouldThrowWhenPublicProfileNotFound() {
        //given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        //when / then
        assertThrows(UserNotFoundException.class, () -> userService.getPublicProfile(userId));
    }

    // ── deactivateUser ────────────────────────────────────────────────────────

    @Test
    void shouldDeactivateUserWhenFound() {
        //given
        UUID userId = UUID.randomUUID();
        var user = User.create("jan@example.com", "Jan");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        //when
        userService.deactivateUser(userId);

        //then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertFalse(captor.getValue().isActive());
    }

    @Test
    void shouldThrowWhenDeactivatingNonExistentUser() {
        //given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        //when / then
        assertThrows(UserNotFoundException.class, () -> userService.deactivateUser(userId));
    }
}
