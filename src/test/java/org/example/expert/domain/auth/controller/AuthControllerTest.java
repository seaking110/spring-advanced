package org.example.expert.domain.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.auth.service.AuthService;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void signup_성공() throws Exception {
        // given
        String email = "qwer@1234";
        String password = "password";
        UserRole userRole = UserRole.USER;
        String bearerToken = "bearerToken";
        SignupRequest request = new SignupRequest(email, password, userRole);
        SignupResponse response = new SignupResponse(bearerToken);
        // when
        given(authService.signup(any(SignupRequest.class))).willReturn(response);
        // then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bearerToken").value(bearerToken));
    }

    @Test
    void signup_IRE_실패() throws Exception {
        // given
        String email = "qwer@1234";
        String password = "password";
        UserRole userRole = UserRole.USER;
        String bearerToken = "bearerToken";
        SignupRequest request = new SignupRequest(email, password, userRole);
        SignupResponse response = new SignupResponse(bearerToken);
        // when
        given(authService.signup(any(SignupRequest.class))).willThrow(InvalidRequestException.class);
        // then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void sighin_성공() throws Exception {
        // given
        String email = "qwer@1234";
        String password = "password";
        String bearerToken = "bearerToken";
        SigninRequest request = new SigninRequest(email, password);
        SigninResponse response = new SigninResponse(bearerToken);
        // when
        given(authService.signin(any(SigninRequest.class))).willReturn(response);
        // then
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bearerToken").value(bearerToken));
    }

    @Test
    void sighin_IRE_실패() throws Exception {
        // given
        String email = "qwer@1234";
        String password = "password";
        SigninRequest request = new SigninRequest(email, password);
        // when
        given(authService.signin(any(SigninRequest.class))).willThrow(InvalidRequestException.class);
        // then
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void sighin_AE_실패() throws Exception {
        // given
        String email = "qwer@1234";
        String password = "password";
        SigninRequest request = new SigninRequest(email, password);
        // when
        given(authService.signin(any(SigninRequest.class))).willThrow(AuthException.class);
        // then
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is4xxClientError());
    }
}