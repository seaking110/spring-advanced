package org.example.expert.domain.user.service;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAdminService userAdminService;

    @Test
    void User의_userRole_변경하기() {
        // given
        User user = new User("qwer@1234","password", UserRole.USER);
        long userId = 1L;
        ReflectionTestUtils.setField(user, "id", userId);
        UserRoleChangeRequest request = new UserRoleChangeRequest("ADMIN");
        UserRole userRole = UserRole.ADMIN;
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        // when
        userAdminService.changeUserRole(userId, request);
        // then
        assertEquals(userRole, user.getUserRole());
    }

    @Test
    void 존재하지_않는_역할로_변경시() {
        // given
        long userId = 1L;
        User user = new User("qwer@1234","password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);
        UserRoleChangeRequest request = new UserRoleChangeRequest("MASTER");
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        // when & then
        assertThrows(InvalidRequestException.class, () -> userAdminService.changeUserRole(userId, request));
    }
    @Test
    void 존재하지_않는_유저를_조회시() {
        // given
        long userId = 100L;
        UserRoleChangeRequest request = new UserRoleChangeRequest("ADMIN");
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());
        // when & then
        assertThrows(InvalidRequestException.class,
                () -> userAdminService.changeUserRole(userId, request),
                "User not found"
        );
    }


}