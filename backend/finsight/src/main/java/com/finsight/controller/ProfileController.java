
package com.finsight.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProfileController {

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/profile")
    public String getProfile(Authentication authentication) {
        return "Authenticated successfully: "
                + authentication.getName();
    }
}
