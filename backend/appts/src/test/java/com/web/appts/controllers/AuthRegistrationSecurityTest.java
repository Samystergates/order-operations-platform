package com.web.appts.controllers;

import com.web.appts.configurations.CustomUserDetailService;
import com.web.appts.configurations.JwtAuthenticationEntryPoint;
import com.web.appts.configurations.JwtAuthenticationFilter;
import com.web.appts.configurations.JwtTokenHelper;
import com.web.appts.configurations.SecurityConfig;
import com.web.appts.repositories.UserRepo;
import com.web.appts.services.OrderMaintenanceService;
import com.web.appts.services.UserService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAuthenticationFilter.class})
class AuthRegistrationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomUserDetailService userDetailsService;

    @MockBean
    private JwtTokenHelper jwtTokenHelper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepo userRepo;

    @MockBean
    private OrderMaintenanceService orderMaintenanceService;

    @MockBean
    private ModelMapper mapper;

    @Test
    void registrationRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrationRequiresAdminRole() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register").with(user("operator").roles("USER")))
                .andExpect(status().isForbidden());
    }
}
