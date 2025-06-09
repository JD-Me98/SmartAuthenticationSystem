package com.megatechs.SmartAuthenticationSystem.services;

import com.megatechs.SmartAuthenticationSystem.models.AppUser;
import com.megatechs.SmartAuthenticationSystem.models.DTOs.AuthResponse;
import com.megatechs.SmartAuthenticationSystem.models.DTOs.LoginRequest;
import com.megatechs.SmartAuthenticationSystem.models.DTOs.RefreshTokenRequest;
import com.megatechs.SmartAuthenticationSystem.models.DTOs.RegisterRequest;
import com.megatechs.SmartAuthenticationSystem.models.RefreshToken;
import com.megatechs.SmartAuthenticationSystem.models.UserRole;
import com.megatechs.SmartAuthenticationSystem.repository.RefreshTokenRepository;
import com.megatechs.SmartAuthenticationSystem.repository.RoleRepository;
import com.megatechs.SmartAuthenticationSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.ref.Reference;
import java.util.Date;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;


    public void registerUser(RegisterRequest request) {
        UserRole userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        AppUser user = new AppUser();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(userRole));

        userRepository.save(user);
    }

    public ResponseEntity<AuthResponse> login(LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            AppUser user = userRepository.findByEmail(request.getEmail()).orElseThrow();

            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            // Save refresh token
            RefreshToken token = new RefreshToken();
            token.setUser(user);
            token.setToken(refreshToken);
            token.setExpiryDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24));
            refreshTokenRepository.save(token);


            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    public ResponseEntity<AuthResponse> refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (storedToken.getExpiryDate().before(new Date())) {
            throw new RuntimeException("Refresh token expired");
        }

        AppUser user = storedToken.getUser();
        String newAccessToken = jwtService.generateToken(user);

        // Optional: generate and update refresh token as well
        String newRefreshToken = jwtService.generateRefreshToken(user);
        storedToken.setToken(newRefreshToken);
        storedToken.setExpiryDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)); // 24h
        refreshTokenRepository.save(storedToken);

        return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken));
    }

}
