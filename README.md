# 📝 Spring Advanced To-Do Management

## 📌 프로젝트 소개
이 프로젝트는 **To-Do 관리 시스템**으로, **JWT 인증 기반의 회원가입 및 로그인 기능**을 포함합니다.  
특징적인 요소로 **외부 API를 이용한 날씨 정보 활용**, **To-Do에 대한 댓글 기능**, **매니저 지정 기능**이 있으며,  
**모든 도메인 코드에 대한 테스트 코드 작성**이 핵심 목표인 프로젝트입니다.

## 🚀 주요 기능
- **회원 관리**
  - JWT를 이용한 회원가입 및 로그인
  - 사용자 역할(Role) 변경 기능 (Admin)

- **To-Do 관리**
  - To-Do 생성, 조회
  - To-Do 등록 시 **외부 API를 이용해 날씨 정보 자동 추가**
  - To-Do에 **매니저 지정** 및 **관리자 해제 기능**

- **댓글 기능**
  - 특정 To-Do에 대한 댓글 작성 및 조회
  - 관리자(Admin)에 의한 댓글 삭제 기능

- **테스트 코드 작성**
  - **모든 도메인 코드에 대한 테스트 코드 작성 완료**
  - **테스트 커버리지 확인을 위한 CI 적용**
  - 아래는 테스트 커버리지 예제 이미지입니다.

## 📊 테스트 커버리지
![Image](https://github.com/user-attachments/assets/199502ae-2781-430c-a352-b17934a46a89)

## 🏗️ 개발 환경  
- **🛠 Spring Boot**: 3.x  
- **☕ Java**: 17  
- **🗄 JPA & H2 Database**  
- **✅ 테스트**: JUnit 5 & Mockito  
- **🌍 외부 API 연동**: RestTemplate (날씨 정보)

## 🛠️ 주요 API 명세

### 🔐 Auth (회원 인증)
| Method | Endpoint | 설명 |
|--------|----------|------|
| `POST` | `/auth/signup` | 회원가입 |
| `POST` | `/auth/signin` | 로그인 |

### 👤 User (사용자)
| Method | Endpoint | 설명 |
|--------|----------|------|
| `GET` | `/users/{userId}` | 사용자 정보 조회 |
| `PUT` | `/users` | 비밀번호 변경 |

### 📌 To-Do
| Method | Endpoint | 설명 |
|--------|----------|------|
| `POST` | `/todos` | To-Do 생성 (날씨 정보 포함) |
| `GET` | `/todos` | To-Do 목록 조회 |
| `GET` | `/todos/{todoId}` | 특정 To-Do 조회 |

### 💬 Comment (댓글)
| Method | Endpoint | 설명 |
|--------|----------|------|
| `POST` | `/todos/{todoId}/comments` | 특정 To-Do에 댓글 작성 |
| `GET` | `/todos/{todoId}/comments` | 특정 To-Do의 댓글 목록 조회 |
| `DELETE` | `/admin/comments/{commentId}` | 관리자에 의한 댓글 삭제 |

### 🏷️ Manager (매니저 기능)
| Method | Endpoint | 설명 |
|--------|----------|------|
| `POST` | `/todos/{todoId}/managers` | To-Do에 매니저 지정 |
| `GET` | `/todos/{todoId}/managers` | 특정 To-Do의 매니저 목록 조회 |
| `DELETE` | `/todos/{todoId}/managers/{managerId}` | 매니저 해제 |

## 🧪 테스트 코드 예제
```java
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


