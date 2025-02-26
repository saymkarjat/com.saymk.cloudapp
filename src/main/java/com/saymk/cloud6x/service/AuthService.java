package com.saymk.cloud6x.service;

import com.saymk.cloud6x.dto.AuthResponseDTO;
import com.saymk.cloud6x.dto.UserRequestDTO;
import com.saymk.cloud6x.exception.UserAlreadyExistException;
import com.saymk.cloud6x.model.User;
import com.saymk.cloud6x.repository.UserRepository;
import com.saymk.cloud6x.security.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    @Value("${jwt.access_token_expiration_minutes}")
    private int accessTokenExpirationMinutes;
    @Value("${jwt.refresh_token_expiration_days}")
    private int refreshTokenExpirationDays;

    public void signUp(UserRequestDTO userRequestDTO) {
        User user = User.builder()
                .username(userRequestDTO.username())
                .password(passwordEncoder.encode(userRequestDTO.password()))
                .role(Role.USER)
                .build();
        try {
            userRepository.save(user);
            log.info("User {} successfully registered", userRequestDTO.username());
        } catch (DataIntegrityViolationException e) {
            log.warn("Registration failed: user {} already exists", userRequestDTO.username());
            throw new UserAlreadyExistException("User already exist");
        }
    }

    public AuthResponseDTO signIn(UserRequestDTO userRequestDTO) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userRequestDTO.username(), userRequestDTO.password()));

        SecurityContextHolder.getContext().setAuthentication(authenticate);

        String accessToken = jwtService.generateAccessToken(userRequestDTO.username());
        String refreshToken = jwtService.generateRefreshToken(userRequestDTO.username());

        return new AuthResponseDTO(
                accessToken,
                refreshToken,
                TimeUnit.MINUTES.toSeconds(accessTokenExpirationMinutes),
                TimeUnit.DAYS.toSeconds(refreshTokenExpirationDays)
        );
    }
}
