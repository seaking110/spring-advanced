package org.example.expert.domain.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ManagerController.class)
class ManagerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ManagerService managerService;

    @Test
    void 관리자_등록_성공() throws Exception {
        // given
        AuthUser authUser = new AuthUser(1L, "qwer@1234", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        long todoId = 1L;
        ManagerSaveRequest request = new ManagerSaveRequest(2L);
        ManagerSaveResponse response = new ManagerSaveResponse(1L, new UserResponse(1L, "qwer@1234"));
        given(managerService.saveManager(any(AuthUser.class), anyLong(), any(ManagerSaveRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/todos/{todoId}/managers", todoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userId", user.getId())
                        .requestAttr("email",user.getEmail())
                        .requestAttr("userRole", user.getUserRole().name())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("qwer@1234"));
    }


    @Test
    void 관리자_목록_조회() throws Exception {
        // given
        long todoId = 1L;
        long managerId1 = 1L;
        long managerId2 = 2L;
        UserResponse user1 = new UserResponse(1L ,"qwert@12345");
        UserResponse user2 = new UserResponse(2L ,"qwer@1234");
        List<ManagerResponse> list = List.of(
                new ManagerResponse(managerId1, user1),
                new ManagerResponse(managerId2, user2)
        );
        given(managerService.getManagers(todoId)).willReturn(list);

        // when & then
        mockMvc.perform(get("/todos/{todoId}/managers", todoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(managerId1))
                .andExpect(jsonPath("$[0].user.email").value("qwert@12345"))
                .andExpect(jsonPath("$[1].id").value(managerId2))
                .andExpect(jsonPath("$[1].user.email").value("qwer@1234"));
    }

    @Test
    void 관리자_삭제_성공() throws Exception {
        // given
        AuthUser authUser = new AuthUser(1L, "qwer@1234", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        long todoId = 1L;
        long managerId = 1L;

        doNothing().when(managerService).deleteManager(any(AuthUser.class), anyLong(), anyLong());

        // when & then
        mockMvc.perform(delete("/todos/{todoId}/managers/{managerId}", todoId, managerId)
                        .requestAttr("userId", user.getId())
                        .requestAttr("email",user.getEmail())
                        .requestAttr("userRole", user.getUserRole().name())
                )
                .andExpect(status().isOk());

    }

}