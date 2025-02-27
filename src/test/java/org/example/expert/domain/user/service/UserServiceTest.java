package org.example.expert.domain.user.service;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void User를_ID로_조회_성공() {
        // given
        User user = new User("qwer@1234","password", UserRole.USER);
        long userId = 1L;
        ReflectionTestUtils.setField(user, "id", userId);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        // when
        UserResponse userResponse = userService.getUser(userId);

        // then
        assertEquals(userId, userResponse.getId());
        assertNotNull(userResponse);
    }

    @Test
    void User를_ID로_조회_실패() {
        // given
        long userId = 100L;
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(InvalidRequestException.class,
                () -> userService.getUser(userId),"User not found");
    }

    @Test
    void Password_변경_성공() {
        // given
        long userId = 1L;
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String encodedOldPassword = "encodedOldPassword";
        String encodedNewPassword = "encodedNewPassword";
        UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);
        User user = new User("qwer@1234", encodedOldPassword, UserRole.USER);
        ReflectionTestUtils.setField(user,"id", userId);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(newPassword, encodedOldPassword)).willReturn(false);
        given(passwordEncoder.matches(oldPassword, encodedOldPassword)).willReturn(true);
        given(passwordEncoder.encode(newPassword)).willReturn(encodedNewPassword);

        // when
        userService.changePassword(userId, request);

        // then
        assertEquals(encodedNewPassword, user.getPassword());
    }

    @Test
    void Password_변경전_ID로_조회_실패() {
        // given
        long userId = 100L;
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(InvalidRequestException.class,
                () -> userService.changePassword(userId, request),
                "User not found"
        );
    }

    @Test
    void newPassword와_oldPassword가_같음() {
        // given
        long userId = 1L;
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String encodedOldPassword = "encodedOldPassword";
        UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);
        User user = new User("qwer@1234", encodedOldPassword, UserRole.USER);
        ReflectionTestUtils.setField(user,"id", userId);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(newPassword, encodedOldPassword)).willReturn(true);

        // when & then
        assertThrows(InvalidRequestException.class,
                () -> userService.changePassword(userId, request),
                "새 비밀번호는 기존 비밀번호와 같을 수 없습니다."
        );
    }

    @Test
    void Password가_틀림() {
        // given
        long userId = 1L;
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String encodedOldPassword = "encodedOldPassword";
        String encodedNewPassword = "encodedNewPassword";
        UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);
        User user = new User("qwer@1234", encodedOldPassword, UserRole.USER);
        ReflectionTestUtils.setField(user,"id", userId);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(newPassword, encodedOldPassword)).willReturn(false);
        given(passwordEncoder.matches(oldPassword, encodedOldPassword)).willReturn(false);

        // when & then
        assertThrows(InvalidRequestException.class,
                () -> userService.changePassword(userId, request),
                "잘못된 비밀번호입니다."
        );
    }
}