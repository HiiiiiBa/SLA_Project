package com.sla.monitoring.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sla.monitoring.dto.request.LoginRequest;
import com.sla.monitoring.dto.request.RegisterRequest;
import com.sla.monitoring.dto.response.AuthenticationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Register creates a new user and returns JWT tokens")
    void registerSuccess() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@sla.com")
                .password("Password1!")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    @DisplayName("Login returns valid JWT for admin user")
    void loginSuccess() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("admin@sla.com")
                .password("Admin123!")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @DisplayName("Valid JWT allows access to protected endpoint")
    void validJwtAccessProtectedEndpoint() throws Exception {
        registerUser("change.pwd@sla.com", "Password1!");
        String accessToken = loginAndGetAccessToken("change.pwd@sla.com", "Password1!");

        mockMvc.perform(put("/api/auth/change-password")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"Password1!","newPassword":"Password2!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Invalid JWT is rejected on protected endpoint")
    void invalidJwtRejected() throws Exception {
        mockMvc.perform(put("/api/auth/change-password")
                        .header("Authorization", "Bearer invalid.token.value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"Admin123!","newPassword":"Admin1234!"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected endpoint requires authentication")
    void protectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(put("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"Admin123!","newPassword":"Admin1234!"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("USER role cannot access ADMIN-only API routes")
    void userRoleForbiddenOnAdminRoutes() throws Exception {
        registerUser("role.test@sla.com", "Password1!");
        String userToken = loginAndGetAccessToken("role.test@sla.com", "Password1!");

        mockMvc.perform(post("/api/admin/settings")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Actuator health endpoint is public")
    void actuatorHealthIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    private void registerUser(String email, String password) throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .password(password)
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));
    }

    private String loginAndGetAccessToken(String email, String password) throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        AuthenticationResponse response = objectMapper.readValue(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("data").traverse(),
                AuthenticationResponse.class);

        assertThat(response.getAccessToken()).isNotBlank();
        return response.getAccessToken();
    }
}
