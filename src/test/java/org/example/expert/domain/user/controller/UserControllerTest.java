package org.example.expert.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void User_단건_조회 () throws Exception {
        // given
        long userId = 1L;
        String email = "qwer@1234";
        given(userService.getUser(userId)).willReturn(new UserResponse(userId, email));

        // when & then
        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void User_password_change() throws Exception {
        // given
        long userId =1L;
        String oldPassword = "oldPassword123";
        String newPassword = "newPassword123";
        User user = new User("qwer@1234", oldPassword, UserRole.ADMIN);
        UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);


        doNothing().when(userService).changePassword(userId, request);

        // when & then
        mockMvc.perform(put("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .requestAttr("userId", userId)
                    .requestAttr("email", user.getEmail())
                    .requestAttr("userRole", user.getUserRole().name())
                )
                .andExpect(status().isOk());
    }

    @Test
    void UserPassword_변경_실패() throws Exception {
        // given
        String oldPassword = "Qwer1234";
        String newPassword = "Qwert12345";
        UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);

        willThrow(InvalidRequestException.class).given(userService).changePassword(anyLong(), any(UserChangePasswordRequest.class));
        // when & then
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}