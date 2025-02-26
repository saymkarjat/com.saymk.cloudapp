package com.saymk.cloud6x.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(
        @NotNull(message = "Поле не может быть null.")
        @NotBlank(message = "Поле не может быть пустым или содержать только пробелы.")
        @Size(min = 5, max = 15, message = "Логин должен быть от 5 до 10 символов.")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Логин должен содержать только латинские буквы и цифры.")
        String username,
        @NotNull(message = "Поле не может быть null.")
        @NotBlank(message = "Поле не может быть пустым или содержать только пробелы.")
        @Size(min = 5, max = 25, message = "Пароль должен иметь от 8 до 25 символов")
        String password) {
}
