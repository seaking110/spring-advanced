package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private WeatherClient weatherClient;

    @InjectMocks
    private TodoService todoService;

    @Test
    void 할일_저장_성공() {
        // given
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        String title = "제목";
        String contents = "내용";
        TodoSaveRequest request = new TodoSaveRequest(title, contents);
        String weather = "날씨가 좋아요";
        Todo todo = new Todo(title, contents, weather, user);

        given(weatherClient.getTodayWeather()).willReturn("날씨가 좋아요");
        given(todoRepository.save(any())).willReturn(todo);

        // when
        TodoSaveResponse result = todoService.saveTodo(authUser, request);

        // then
        assertNotNull(result);
    }

    @Test
    void 할일_저장_중_날씨_찾기_실패() {
        // given
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        String title = "제목";
        String contents = "내용";
        TodoSaveRequest request = new TodoSaveRequest(title, contents);

        // when
        given(weatherClient.getTodayWeather()).willThrow(ServerException.class);

        // then
        assertThrows(ServerException.class, () -> todoService.saveTodo(authUser, request));
    }

    @Test
    void 일정_전체_조회() {
        // given
        User user = new User("qwer@1234","password",UserRole.USER);
        int page = 1;
        int size = 5;
        Todo todo1 = new Todo(
                "title1",
                "contents1",
                "sunny1",
                user
        );
        Todo todo2 = new Todo(
                "title2",
                "contents2",
                "sunny2",
                user
        );
        List<Todo> list = List.of(todo1, todo2);
        Page<Todo> todoPage =
                new PageImpl<>(list, PageRequest.of(page -1, size),2);

        given(todoRepository.findAllByOrderByModifiedAtDesc(
                PageRequest.of(page -1, size)))
                .willReturn(todoPage);

        // when
        Page<TodoResponse> responses = todoService.getTodos(page,size);

        // then
        assertNotNull(responses);
        assertEquals(2, responses.getTotalElements());
        assertEquals("title2", responses.getContent().get(1).getTitle());

    }

    @Test
    void 빈_일정_조회() {
        // given
        int page = 1;
        int size = 5;
        List<Todo> list = List.of();
        Page<Todo> todoPage = new PageImpl<>(list, PageRequest.of(page-1, size), 0);
        given(todoRepository.findAllByOrderByModifiedAtDesc(
                PageRequest.of(page -1, size)))
                .willReturn(todoPage);
        // when
        Page<TodoResponse> responses = todoService.getTodos(page,size);

        // then
        assertNotNull(responses);
        assertEquals(0, responses.getTotalElements());
    }

    @Test
    void 단일_일정_조회_성공() {
        // given
        Todo todo = new Todo("title", "contents", "Sunny",
                new User("qwer@1234","password",UserRole.USER));
        long todoId = 1L;
        ReflectionTestUtils.setField(todo, "id", todoId);
        given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.of(todo));
        // when
        TodoResponse todoResponse = todoService.getTodo(todoId);

        // then
        assertEquals(todoId, todoResponse.getId());
        assertNotNull(todoResponse);
    }

    @Test
    void 단일_일정_조회_실패() {
        // given
        long todoId = 100L;
        given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(InvalidRequestException.class,
                () -> todoService.getTodo(todoId),"Todo not found");
    }

}