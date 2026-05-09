package com.estatex.application.user;

import com.estatex.application.port.out.FileStoragePort;
import com.estatex.domain.exception.DomainException;
import com.estatex.domain.exception.UserNotFoundException;
import com.estatex.domain.user.User;
import com.estatex.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ── UC-1.1  Register ──────────────────────────────────────────────────────

    public record RegisterCommand(String email, String displayName) {}

    public UserResult register(RegisterCommand cmd) {
        if (userRepository.existsByEmail(cmd.email())) {
            throw new DomainException("Email is already registered");
        }
        var user = User.create(cmd.email(), cmd.displayName());
        user = userRepository.save(user);
        return UserResult.from(user, 0);
    }

    // ── UC-1.2  Login ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserResult login(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("No user found with this email"));
        return UserResult.from(user, userRepository.countActiveListings(user.getId()));
    }

    // ── UC-2.1  Get / update profile ──────────────────────────────────────────

    public record UpdateProfileCommand(UUID userId, String displayName, String phone) {}

    public UserResult updateProfile(UpdateProfileCommand cmd) {
        var user = userRepository.findById(cmd.userId())
                .orElseThrow(() -> new UserNotFoundException(cmd.userId()));
        user.updateProfile(cmd.displayName(), cmd.phone());
        user = userRepository.save(user);
        return UserResult.from(user, userRepository.countActiveListings(user.getId()));
    }

    // ── UC-2.2  View public profile ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserResult getPublicProfile(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return UserResult.from(user, userRepository.countActiveListings(userId));
    }

    @Transactional(readOnly = true)
    public UserResult getCurrentUser(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return UserResult.from(user, userRepository.countActiveListings(userId));
    }

    // ── UC-10.2  Admin deactivate user ────────────────────────────────────────

    public void deactivateUser(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.deactivate();
        userRepository.save(user);
    }
}
