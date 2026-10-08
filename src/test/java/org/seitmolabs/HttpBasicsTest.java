package org.seitmolabs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.List;
import java.util.Optional;
import org.seitmolabs.config.WebConfig;
import org.seitmolabs.common.exceptions.GlobalExceptionHandler;
import org.seitmolabs.modules.auth.controller.AuthController;
import org.seitmolabs.modules.auth.filters.SimpleAuthFilter;
import org.seitmolabs.modules.auth.service.AuthService;
import org.seitmolabs.modules.user.domain.User;
import org.seitmolabs.modules.user.enums.Role;
import org.seitmolabs.modules.user.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpBasicsTest {

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        UserRepository userRepository = mock(UserRepository.class);
        User admin = User.builder()
                .id(1L)
                .username("admin")
                .password("123")
                .displayName("Администратор")
                .role(Role.ROLE_ADMIN)
                .build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        AuthService authService = new AuthService(userRepository);
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new WebConfig().corsFilter(List.of("http://localhost:3000")).getFilter(),
                        new SimpleAuthFilter(authService))
                .build();
    }

    @Test
    void loginReadsJsonBody() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.displayName").value("Администратор"));
    }

    @Test
    void wrongPasswordReturnsUnauthorized() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRequestReturnsUnauthorizedInsteadOfThrowing() throws Exception {
        mvc.perform(post("/api/v1/notifications").header("Origin", "http://localhost:3000"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    void preflightAllowsPatchAndCredentialHeaders() throws Exception {
        mvc.perform(options("/api/v1/categories/1")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "PATCH")
                        .header("Access-Control-Request-Headers", "X-Username,X-Password"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }
}
