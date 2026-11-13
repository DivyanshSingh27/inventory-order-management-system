package com.auth.security;

import com.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("Public endpoints: /api/v1/auth/register and /api/v1/auth/login are accessible anonymously")
    void publicEndpoints_accessibleWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest()); // validation fails, not 401/403

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest()); // validation fails, not 401/403
    }

    @Test
    @DisplayName("Public endpoints: /actuator/health is accessible anonymously")
    void healthEndpoint_accessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Security: Anonymous call to protected endpoint returns 401 in ApiResponse JSON format")
    void anonymousCallToProtectedEndpoint_returns401InApiResponse() throws Exception {
        mockMvc.perform(get("/api/v1/auth/validate"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    @DisplayName("Security: 401 response matches ApiResponse JSON body shape with timestamp and success=false")
    void anonymousCall_401Response_matchesApiResponseShape() throws Exception {
        mockMvc.perform(get("/api/v1/auth/validate"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Unauthorized"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("Security: Authenticated caller with USER role calling ADMIN endpoint returns 403 (not 500)")
    void userRoleCallingAdminEndpoint_returns403InApiResponse_not500() throws Exception {
        String userToken = jwtTokenProvider.generateToken(1L, "user@example.com", "USER");

        mockMvc.perform(get("/api/v1/test/admin-only")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Forbidden"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("Security: Authenticated caller with ADMIN role calling ADMIN endpoint succeeds (200)")
    void adminRoleCallingAdminEndpoint_succeedsWith200() throws Exception {
        String adminToken = jwtTokenProvider.generateToken(2L, "admin@example.com", "ADMIN");

        mockMvc.perform(get("/api/v1/test/admin-only")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("admin-data"));
    }
}
