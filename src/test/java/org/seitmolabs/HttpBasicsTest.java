package org.seitmolabs;

import org.junit.jupiter.api.Test;
import java.util.List;
import org.seitmolabs.config.WebConfig;
import org.seitmolabs.modules.auth.controller.AuthController;
import org.seitmolabs.modules.auth.filters.SimpleAuthFilter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HttpBasicsTest {

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new AuthController())
            .addFilters(new WebConfig().corsFilter(List.of("http://localhost:3000")).getFilter(),
                    new SimpleAuthFilter())
            .build();

    @Test
    void loginReadsJsonBody() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"123\"}"))
                .andExpect(status().isOk());
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
