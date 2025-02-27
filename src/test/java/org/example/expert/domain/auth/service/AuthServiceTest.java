package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void signup_성공() {
        // given
        String email = "qwer@1234";
        String password = "password";
        String encodedPassword = "encodedPassword";
        String bearerToken = "bearerToken";
        UserRole userRole = UserRole.USER;
        User user = new User(email, encodedPassword, userRole);
        ReflectionTestUtils.setField(user, "id", 1L);
        SignupRequest request = new SignupRequest(email, password, userRole);

        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn(encodedPassword);
        given(userRepository.save(any(User.class))).willReturn(user);
        given(jwtUtil.createToken(anyLong(), anyString(), any(UserRole.class))).willReturn(bearerToken);

        // when
        SignupResponse result = authService.signup(request);

        // then
        assertNotNull(result);
        assertEquals(bearerToken, result.getBearerToken());
    }

    @Test
    void signup_이메일_중복_실패() {
        //given
        String email = "qwer@1234";
        String password = "password";
        UserRole userRole = UserRole.USER;
        SignupRequest request = new SignupRequest(email, password, userRole);
        given(userRepository.existsByEmail(anyString())).willReturn(true);
        // when & then
        assertThrows(InvalidRequestException.class, ()-> authService.signup(request));
    }


    @Test
    void signin_성공() {
        // given
        String email = "qwer@1234";
        String password = "password";
        String encodedPassword = "encodedPassword";
        String bearerToken = "bearerToken";
        UserRole userRole = UserRole.USER;
        User user = new User(email, encodedPassword, userRole);
        ReflectionTestUtils.setField(user, "id", 1L);
        SigninRequest request = new SigninRequest(email, password);

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtUtil.createToken(anyLong(), anyString(), any(UserRole.class))).willReturn(bearerToken);
        // when
        SigninResponse result = authService.signin(request);
        // then
        assertNotNull(result);
        assertEquals(bearerToken, result.getBearerToken());
    }

    @Test
    void 미가입_유저() {
        //given
        String email = "qwer@1234";
        String password = "password";
        SigninRequest request = new SigninRequest(email, password);

        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
        // when & then
        assertThrows(InvalidRequestException.class, ()-> authService.signin(request));
    }

    @Test
    void signin_비밀번호_오류() {
        //given
        String email = "qwer@1234";
        String password = "password";
        String encodedPassword = "encodedPassword";
        UserRole userRole = UserRole.USER;
        User user = new User(email, encodedPassword, userRole);
        SigninRequest request = new SigninRequest(email, password);

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);
        // when & then
        assertThrows(AuthException.class, ()-> authService.signin(request));
    }
}