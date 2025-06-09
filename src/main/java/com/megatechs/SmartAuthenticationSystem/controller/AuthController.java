package com.megatechs.SmartAuthenticationSystem.controller;

import com.megatechs.SmartAuthenticationSystem.models.DTOs.AuthResponse;
import com.megatechs.SmartAuthenticationSystem.models.DTOs.LoginRequest;
import com.megatechs.SmartAuthenticationSystem.models.DTOs.RefreshTokenRequest;
import com.megatechs.SmartAuthenticationSystem.models.DTOs.RegisterRequest;
import com.megatechs.SmartAuthenticationSystem.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        authService.registerUser(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }

}
