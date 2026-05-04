# Spring Boot Testing Guide

This guide explains the testing strategy used in the Code Arena backend, focusing on how Spring Boot, JUnit 5, and Mockito are used to ensure system reliability.

## 1. Testing Philosophy
We follow the **Testing Pyramid** approach:
- **Unit Tests**: Test individual classes in isolation using Mockito. Fast and frequent.
- **Integration Tests**: Test the full stack (Controller → Service → Repository) with a real Spring context. Slower but more comprehensive.

---

## 2. Unit Testing (`AuthServiceTest.java`)

Unit tests focus on the business logic of a single class, mocking all external dependencies.

### Key Annotations
- `@ExtendWith(MockitoExtension.class)`: Initializes Mockito and handles mock lifecycle.
- `@Mock`: Creates a lightweight surrogate object that mimics a dependency (e.g., `UserService`).
- `@InjectMocks`: Creates the real instance of the class under test and automatically injects all `@Mock` fields into it.

### Pattern: Arrange-Act-Assert (AAA)
```java
@Test
void register_duplicateUsername_throwsIllegalArgument() {
    // 1. Arrange: Setup mock behavior
    when(userService.existsByUsername("player1")).thenReturn(true);

    // 2. Act + 3. Assert: Execute and verify outcome
    assertThatThrownBy(() -> authService.register(req))
            .isInstanceOf(IllegalArgumentException.class);
}
```

---

## 3. Integration Testing (`AuthIntegrationTest.java`)

Integration tests verify that different components of the application work together correctly, including the Web layer and Database layer.

### Key Annotations
- `@SpringBootTest`: Loads the complete Application Context.
- `@AutoConfigureMockMvc`: Configures `MockMvc` for testing HTTP endpoints without starting a real server (faster than Selenium/RESTAssured).
- `@ActiveProfiles("test")`: Uses `application-test.properties` for test-specific configuration (e.g., H2 database).
- `@MockitoBean`: (Spring Boot 3.4+) Replaces a real bean in the Spring context with a Mockito mock. Used in our tests to mock Redis interactions.

### Testing the Web Layer
We use `MockMvc` to perform requests and verify responses:
```java
mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginReq)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists());
```

---

## 4. Specific Test Implementations

### Mocking Redis in Integration Tests
Since we use Redis for token blacklisting, we mock `StringRedisTemplate` in `AuthIntegrationTest` to avoid needing a real Redis instance running during tests.
- We use a `ConcurrentHashMap` inside `@BeforeEach` to simulate Redis storage.
- We stub `hasKey` and `set` operations to read/write from this map.

### Testing Expired Tokens
To test expired tokens, we inject the real `JwtService` and use its public `generateToken` method with a negative expiration time:
```java
String expiredToken = jwtService.generateToken(claims, userDetails, -10000, "refresh");
```

---

## 5. How to Run Tests

### Running all tests
```bash
./mvnw test
```

### Running a specific test class
```bash
./mvnw test -Dtest=AuthIntegrationTest
```

### Running a specific test method
```bash
./mvnw test -Dtest=AuthIntegrationTest#refreshTokenFailsWhenExpired
```
