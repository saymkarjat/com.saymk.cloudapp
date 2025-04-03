package com.saymk.cloud6x.controller;

import com.saymk.cloud6x.TestcontainersConfiguration;
import com.saymk.cloud6x.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;


    @Test
    @DisplayName("Должен вернуть вернуть статус 201, и добавить в базу данных зарегистрированного пользователя")
    public void registerUser() throws Exception {
        String userRequestDTO = """
                {
                    "username": "user1",
                    "password": "12345678"
                }""";

        mockMvc.perform(post("/api/auth/sign-up")
                        .content(userRequestDTO)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        Assertions.assertTrue(userRepository.existsByUsername("user1"));
    }

    @Test
    @DisplayName("Должен вернуть 409, если пользователь уже существует")
    @Sql(statements = "INSERT INTO users (username, password) VALUES ('user2','12345678' )")
    public void shouldThrowExceptionWhenUserAlreadyExists() throws Exception {
        String userRequestDTO = """
                {
                    "username": "user2",
                    "password": "12345678"
                }""";

        mockMvc.perform(post("/api/auth/sign-up")
                        .content(userRequestDTO)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }
}
