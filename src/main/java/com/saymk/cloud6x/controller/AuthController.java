package com.saymk.cloud6x.controller;

import com.saymk.cloud6x.dto.AuthResponseDTO;
import com.saymk.cloud6x.dto.UserRequestDTO;
import com.saymk.cloud6x.service.AuthService;
import com.saymk.cloud6x.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "Регистрация и авторизация с помощью JWT токена")
public class AuthController {
    private static final String cookieKeyForAccessToken = "access_token";
    private static final String cookieKeyForRefreshToken = "refresh_token";
    private final AuthService authService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    @Value("${jwt.access_token_expiration_minutes}")
    private int accessTokenExpirationMinutes;

    @PostMapping("sign-up")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRequestDTO userRequestDTO, HttpServletResponse response) {

        authService.signUp(userRequestDTO);
        AuthResponseDTO authData = authService.signIn(userRequestDTO);

        addCookie(cookieKeyForAccessToken, authData.accessToken(), authData.accessTokenMaxAge(), response);
        addCookie(cookieKeyForRefreshToken, authData.refreshToken(), authData.refreshTokenMaxAge(), response);

        Map<String, String> responseBody = Map.of("username", userRequestDTO.username());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @PostMapping("sign-in")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody UserRequestDTO userRequestDTO, HttpServletResponse response) {

        AuthResponseDTO authData = authService.signIn(userRequestDTO);

        addCookie(cookieKeyForAccessToken, authData.accessToken(), authData.accessTokenMaxAge(), response);
        addCookie(cookieKeyForRefreshToken, authData.refreshToken(), authData.refreshTokenMaxAge(), response);

        Map<String, String> responseBody = Map.of("username", userRequestDTO.username());
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("sign-out")
    public ResponseEntity<?> signOut(@AuthenticationPrincipal UserDetails user, HttpServletResponse response) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        addCookie(cookieKeyForAccessToken, "", 0, response);
        addCookie(cookieKeyForRefreshToken, "", 0, response);
        SecurityContextHolder.clearContext();

        return ResponseEntity.noContent().build();
    }

    @PostMapping("refresh-token")
    public ResponseEntity<?> refresh(@CookieValue(name = cookieKeyForRefreshToken)
                                     String refreshToken,
                                     HttpServletResponse response) {

        UserDetails user = userDetailsService.loadUserByUsername(jwtService.extractUsername(refreshToken));

        if (!jwtService.isValid(refreshToken, user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String accessToken = jwtService.generateAccessToken(user.getUsername());
        addCookie(cookieKeyForAccessToken, accessToken, TimeUnit.MINUTES.toSeconds(accessTokenExpirationMinutes), response);
        return ResponseEntity.ok().build();
    }

    private void addCookie(String cookieKey, String cookieValue, long maxAge, HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieKey, cookieValue);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge((int) maxAge);
        response.addCookie(cookie);
    }
}
