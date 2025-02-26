package com.saymk.cloud6x.dto;

public record AuthResponseDTO(String accessToken, String refreshToken, long accessTokenMaxAge, long refreshTokenMaxAge) {
}
