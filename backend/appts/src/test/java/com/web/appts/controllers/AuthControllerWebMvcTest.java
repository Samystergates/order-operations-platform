package com.web.appts.controllers;

import com.web.appts.configurations.JwtAuthenticationFilter;
import com.web.appts.configurations.JwtTokenHelper;
import com.web.appts.repositories.UserRepo;
import com.web.appts.services.OrderMaintenanceService;
import com.web.appts.services.UserService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenHelper jwtTokenHelper;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepo userRepo;

    @MockBean
    private OrderMaintenanceService orderMaintenanceService;

    @MockBean
    private ModelMapper mapper;

    @Test
    void postMaintenanceMarkExpiredInvokesOrderService() throws Exception {
        doNothing().when(orderMaintenanceService).markOrdersExpired();

        mockMvc.perform(post("/api/v1/auth/maintenance/mark-orders-expired"))
                .andExpect(status().isOk());

        verify(orderMaintenanceService).markOrdersExpired();
    }

    @Test
    void legacyGetMaintenanceMarkExpiredRemainsBackwardCompatible() throws Exception {
        doNothing().when(orderMaintenanceService).markOrdersExpired();

        mockMvc.perform(get("/api/v1/auth/mark-orders-expired"))
                .andExpect(status().isOk());

        verify(orderMaintenanceService).markOrdersExpired();
    }

    @Test
    void postMaintenanceTextRefreshReturnsGone() throws Exception {
        mockMvc.perform(post("/api/v1/auth/maintenance/update-tekst"))
                .andExpect(status().isGone())
                .andExpect(content().string("Legacy order-text refresh is no longer supported. Product-note synchronization remains part of regular order refresh."));

        verifyNoInteractions(orderMaintenanceService);
    }

    @Test
    void legacyGetTextRefreshReturnsGone() throws Exception {
        mockMvc.perform(get("/api/v1/auth/update-tekst"))
                .andExpect(status().isGone());

        verifyNoInteractions(orderMaintenanceService);
    }

    @Test
    void loginRejectsBlankCredentials() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"username\":\" \",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrationRejectsInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Demo User\",\"email\":\"not-an-email\",\"password\":\"long-enough-password\"}"))
                .andExpect(status().isBadRequest());
    }
}
