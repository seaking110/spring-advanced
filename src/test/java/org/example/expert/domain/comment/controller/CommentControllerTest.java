package org.example.expert.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentService;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
class CommentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @Test
    void 댓글_저장_성공() throws Exception{
        // given
        long todoId = 1L;
        String contents = "contents";
        User user = new User("qwer@1234","password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
        CommentSaveRequest request = new CommentSaveRequest(contents);
        CommentSaveResponse response = new CommentSaveResponse(todoId,  contents,  new UserResponse(1L,"qwer@1234"));
        // when
        given(commentService.saveComment(any(AuthUser.class), anyLong() ,any(CommentSaveRequest.class))).willReturn(response);
        // then
        mockMvc.perform(post("/todos/{todoId}/comments", todoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("userId", user.getId())
                        .requestAttr("email", user.getEmail())
                        .requestAttr("userRole", user.getUserRole().name())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.contents").value("contents"))
                .andExpect(jsonPath("$.user.email").value("qwer@1234"));
    }

    @Test
    void 댓글_저장_실패() throws Exception {
        // given
        long todoId = 1L;
        String contents = "contents";
        User user = new User("qwer@1234","password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
        CommentSaveRequest request = new CommentSaveRequest(contents);
        // when
        given(commentService.saveComment(any(AuthUser.class), anyLong() ,any(CommentSaveRequest.class))).willThrow(InvalidRequestException.class);
        // then
        mockMvc.perform(post("/todos/{todoId}/comments", todoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("userId", user.getId())
                .requestAttr("email", user.getEmail())
                .requestAttr("userRole", user.getUserRole().name())
        )
                .andExpect(status().is4xxClientError());
    }

    @Test
    void 댓글_목록_조회() throws Exception {
        // given
        User user = new User("qwer@1234", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
        long todoId = 1L;
        CommentResponse response1 = new CommentResponse(
                1L,
                "contents1",
                new UserResponse(user.getId(), user.getEmail())
        );
        CommentResponse response2 = new CommentResponse(
                2L,
                "contents2",
                new UserResponse(user.getId(), user.getEmail())
        );
        List<CommentResponse> list = List.of(response1, response2);
        given(commentService.getComments(anyLong())).willReturn(list);

        // when & then
        mockMvc.perform(get("/todos/{todoId}/comments", todoId)
                        .requestAttr("userId", user.getId())
                        .requestAttr("email", user.getEmail())
                        .requestAttr("userRole", user.getUserRole())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].contents").value("contents2"))
                .andExpect(jsonPath("$[1].user.email").value("qwer@1234")
                );
    }

}