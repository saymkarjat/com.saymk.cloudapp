package com.saymk.cloud6x.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User API", description = "Управление данными о пользователе")
public class UserController {

    @GetMapping("me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, String> responseBody = Map.of("username", user.getUsername());
        return ResponseEntity.ok().body(responseBody);
    }
}
