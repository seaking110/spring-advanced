package org.example.expert.domain.manager.service;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private ManagerService managerService;


    @Test
    void todo의_user가_null인_경우_예외가_발생한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        long todoId = 1L;
        long managerUserId = 2L;

        Todo todo = new Todo();
        ReflectionTestUtils.setField(todo, "user", null);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
    }

    @Test // 테스트코드 샘플
    void 매니저가_정상적으로_등록된다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 2L;
        User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
        given(managerRepository.save(any(Manager.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        ManagerSaveResponse response = managerService.saveManager(authUser, todoId, managerSaveRequest);

        // then
        assertNotNull(response);
        assertEquals(managerUser.getId(), response.getUser().getId());
        assertEquals(managerUser.getEmail(), response.getUser().getEmail());
    }

    @Test
    void 매니저_등록_실패_등록하려는_유저_미존재() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        long todoId = 1L;
        long managerUserId = 2L;
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title","contents","sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);
        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(managerUserId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("등록하려고 하는 담당자 유저가 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    void 일정_작성자를_관리자로_등록() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        long todoId = 1L;
        long userId = 1L;
        long managerUserId = 1L;
        User user = User.fromAuthUser(authUser);
        ReflectionTestUtils.setField(user, "id", userId);
        Todo todo = new Todo("title","contents","sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);
        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));


        // when
        boolean result = ObjectUtils.nullSafeEquals(todoId, managerUserId);
        // then
        assertTrue(result);
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("일정 작성자는 본인을 담당자로 등록할 수 없습니다.", exception.getMessage());
    }

    @Test
    public void manager_목록_조회_시_Todo가_없다면_IRE_에러를_던진다() {
        // given
        long todoId = 1L;
        given(todoRepository.findById(todoId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.getManagers(todoId));
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test // 테스트코드 샘플
    public void manager_목록_조회에_성공한다() {
        // given
        long todoId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Manager mockManager = new Manager(todo.getUser(), todo);
        List<Manager> managerList = List.of(mockManager);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

        // when
        List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

        // then
        assertEquals(1, managerResponses.size());
        assertEquals(mockManager.getId(), managerResponses.get(0).getId());
        assertEquals(mockManager.getUser().getEmail(), managerResponses.get(0).getUser().getEmail());
    }

    @Test
    void 관리자_삭제_성공() {
        // given
        long managerId = 1L;
        long userId = 1L;
        AuthUser authUser = new AuthUser(1L, "qwer@1234",UserRole.USER);
        User user = User.fromAuthUser(authUser);
        long todoId = 1L;
        Todo todo = new Todo("title", "contents", "sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);
        Manager manager = new Manager(user, todo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        boolean result = ObjectUtils.nullSafeEquals(user.getId(), todo.getUser().getId());
        given(managerRepository.findById(managerId)).willReturn(Optional.of(manager));
        doNothing().when(managerRepository).delete(manager);
        // when
        managerService.deleteManager(authUser, todoId ,managerId);

        // then
        assertTrue(result);
        verify(managerRepository, times(1)).delete((manager));
    }



    @Test
    void 속한_Todo_미식별() {
        // given
        long managerId = 1L;
        long userId = 1L;
        AuthUser authUser = new AuthUser(1L, "qwer@1234",UserRole.USER);
        User user = User.fromAuthUser(authUser);        ReflectionTestUtils.setField(user, "id", userId);
        long todoId = 1L;
        Todo todo = new Todo("title", "contents", "sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);
        Manager manager = new Manager(user, todo);
        ReflectionTestUtils.setField(manager, "id", managerId);
        given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.deleteManager(authUser, todoId, managerId), "Todo not found");
        verify(managerRepository, never()).delete((manager));
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    void 일정_작성자_미식별() {
        // given
        long managerId = 1L;
        long userId = 1L;
        AuthUser authUser = new AuthUser(1L, "qwer@1234",UserRole.USER);
        User user = User.fromAuthUser(authUser);
        long todoId = 1L;
        Todo todo = new Todo("title", "contents", "sunny", user);
        ReflectionTestUtils.setField(todo, "user", null);
        Manager manager = new Manager(user, todo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.deleteManager(authUser, todoId, managerId), "해당 일정을 만든 유저가 유효하지 않습니다.");
        verify(managerRepository, never()).delete((manager));
        assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
    }

    @Test
    void 일정_작성자_불일치() {
        // given
        long managerId = 1L;
        long userId = 1L;
        AuthUser authUser = new AuthUser(1L, "qwer@1234",UserRole.USER);
        User user = User.fromAuthUser(authUser);
        long newUserId = 2L;
        User newUser = new User("qwer@1234","password",UserRole.USER);
        ReflectionTestUtils.setField(newUser, "id", newUserId);
        long todoId = 1L;
        Todo todo = new Todo();
        ReflectionTestUtils.setField(todo, "user", newUser);
        Manager manager = new Manager(user, todo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        boolean result = ObjectUtils.nullSafeEquals(user.getId(), todo.getUser().getId());
        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.deleteManager(authUser, todoId, managerId), "해당 일정을 만든 유저가 유효하지 않습니다.");
        verify(managerRepository, never()).delete((manager));
        assertFalse(result);
        assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());

    }
    @Test
    void 관리자_미식별() {
        // given
        long managerId = 1L;
        long userId = 1L;
        AuthUser authUser = new AuthUser(1L, "qwer@1234",UserRole.USER);
        User user = User.fromAuthUser(authUser);
        long todoId = 1L;
        Todo todo = new Todo("title", "contents", "sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);
        Manager manager = new Manager(user, todo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(managerRepository.findById(managerId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.deleteManager(authUser, todoId, managerId), "Manager not found");
        verify(managerRepository, never()).delete((manager));
        assertEquals("Manager not found", exception.getMessage());
    }

    @Test
    void 해당_일정_관리자_아님() {
        // given
        long managerId = 1L;
        long userId = 1L;
        AuthUser authUser = new AuthUser(1L, "qwer@1234",UserRole.USER);
        User user = User.fromAuthUser(authUser);
        long todoId = 1L;
        Todo todo = new Todo("title", "contents", "sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);
        long newTodoId = 2L;
        Todo newTodo = new Todo("title", "contents", "sunny", user);
        ReflectionTestUtils.setField(todo, "id", newTodoId);
        Manager manager = new Manager(user, newTodo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        boolean result1 = ObjectUtils.nullSafeEquals(user.getId(), todo.getUser().getId());
        given(managerRepository.findById(managerId)).willReturn(Optional.of(manager));
        boolean result2 = ObjectUtils.nullSafeEquals(todo.getId(), manager.getTodo().getId());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.deleteManager(authUser, todoId, managerId), "해당 일정에 등록된 담당자가 아닙니다.");
        verify(managerRepository, never()).delete((manager));
        assertTrue(result1);
        assertEquals("해당 일정에 등록된 담당자가 아닙니다.", exception.getMessage());
    }
}
