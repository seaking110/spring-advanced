package org.example.expert.domain.todo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(TodoController.class)
class TodoControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private TodoService todoService;
    @Test
    void 일정_저장_성공() throws Exception{
        // given
        long todoId = 1L;
        String title = "title";
        String contents = "contents";
        User user = new User("qwer@1234","password",UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
        TodoSaveRequest request = new TodoSaveRequest(title, contents);
        TodoSaveResponse response = new TodoSaveResponse(todoId, title, contents,"sunny", new UserResponse(1L,"qwer@1234"));
        // when
        given(todoService.saveTodo(any(AuthUser.class), any(TodoSaveRequest.class))).willReturn(response);
        // then
        mockMvc.perform(post("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("userId", user.getId())
                .requestAttr("email", user.getEmail())
                .requestAttr("userRole", user.getUserRole().name())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("title"))
                .andExpect(jsonPath("$.contents").value("contents"))
                .andExpect(jsonPath("$.weather").value("sunny"));
    }

    @Test
    void 날짜_오류로_인한_일정_저장_실패() throws Exception {
        //given
        String title = "title";
        String contents = "contents";
        User user = new User("qwer@1234","password",UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
        TodoSaveRequest request = new TodoSaveRequest(title, contents);
        // when
        given(todoService.saveTodo(any(AuthUser.class), any(TodoSaveRequest.class))).willThrow(ServerException.class);
        // then
        mockMvc.perform(post("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("userId", user.getId())
                .requestAttr("email", user.getEmail())
                .requestAttr("userRole", user.getUserRole().name())
        )
                .andExpect(status().is5xxServerError());
    }


    @Test
    void 일정_목록_조회() throws Exception {
        // given
        User user = new User("qwer@1234", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
        TodoResponse response1 = new TodoResponse(
                1L,
                "title1",
                "contents1",
                "sunny",
                new UserResponse(user.getId(), user.getEmail()),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        TodoResponse response2 = new TodoResponse(
                2L,
                "title2",
                "contents2",
                "so bad",
                new UserResponse(user.getId(), user.getEmail()),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        List<TodoResponse> list = List.of(response1, response2);
        Page<TodoResponse> todoPage = new PageImpl<>(list, PageRequest.of(0,5),2);
        given(todoService.getTodos(anyInt(),anyInt())).willReturn(todoPage);

        // when & then
        mockMvc.perform(get("/todos")
                        .requestAttr("userId", user.getId())
                        .requestAttr("email", user.getEmail())
                        .requestAttr("userRole", user.getUserRole())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("title1"))
                .andExpect(jsonPath("$.content[1].contents").value("contents2"))
                .andExpect(jsonPath("$.content[1].weather").value("so bad")
        );
    }

    @Test
    void 빈_일정_목록_조회() throws Exception{
        // given
        int size = 5;
        int page = 1;
        List<TodoResponse> todoResponseList = List.of();
        Page<TodoResponse> todoResponsePage = new PageImpl<>(todoResponseList, PageRequest.of(page-1, size), 0);
        given(todoService.getTodos(anyInt(),anyInt())).willReturn(todoResponsePage);

        // when & then
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void 단일_일정_조회_성공() throws Exception {
        // given
        long todoId = 1L;
        String title = "title";
        String contents = "contents";
        String weather = "sunny";
        UserResponse user = new UserResponse(1L,"qwer@1234");
        TodoResponse todoResponse = new TodoResponse(
                todoId,
                title,
                contents,
                weather,
                user,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        given(todoService.getTodo(anyLong())).willReturn(todoResponse);

        //when && then
        mockMvc.perform(get("/todos/{todoId}",todoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(todoId))
                .andExpect(jsonPath("$.title").value(title));

    }
}