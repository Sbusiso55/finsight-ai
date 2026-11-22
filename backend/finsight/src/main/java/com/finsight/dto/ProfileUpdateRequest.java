package com.finsight.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Fields a user may change. Email and password are deliberately not here. */
public record ProfileUpdateRequest(
        @NotBlank @Size(max = 100) String fullName,
        @Pattern(regexp = "^$|^\\+?[0-9 ]{9,15}$", message = "Invalid phone number") String phoneNumber
) {}