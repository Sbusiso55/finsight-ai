package com.finsight.controller;

import com.finsight.dto.ProfileResponse;
import com.finsight.dto.ProfileUpdateRequest;
import com.finsight.model.User;
import com.finsight.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Always the logged-in user from the JWT, never an id from the request. */
    private User currentUser(Authentication auth) {
        if (auth == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private ProfileResponse toResponse(User u) {
        return new ProfileResponse(u.getFullName(), u.getEmail(), u.getPhoneNumber());
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(Authentication auth) {
        return ResponseEntity.ok(toResponse(currentUser(auth)));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(Authentication auth,
                                                         @Valid @RequestBody ProfileUpdateRequest request) {
        User user = currentUser(auth);
        user.setFullName(request.fullName().trim());
        String phone = request.phoneNumber();
        user.setPhoneNumber(phone == null || phone.isBlank() ? null : phone.trim());
        return ResponseEntity.ok(toResponse(userRepository.save(user)));
    }
}