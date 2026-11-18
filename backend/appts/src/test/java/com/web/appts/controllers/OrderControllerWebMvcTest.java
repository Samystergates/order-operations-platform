package com.web.appts.controllers;

import com.web.appts.DTO.OrderDto;
import com.web.appts.configurations.JwtAuthenticationFilter;
import com.web.appts.services.OrderRefreshWorkflowService;
import com.web.appts.services.OrderService;
import com.web.appts.services.OrderTextSyncService;
import com.web.appts.services.OrderTRAService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private OrderTRAService orderTRAService;

    @MockBean
    private OrderRefreshWorkflowService orderRefreshWorkflowService;

    @MockBean
    private OrderTextSyncService orderTextSyncService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void getOrdersReturnsSortedResponse() throws Exception {
        OrderDto older = new OrderDto();
        older.setOrderNumber("100");
        older.setDeliveryDate("2024-01-01");
        older.setCreationDate("2024-01-01");

        OrderDto newer = new OrderDto();
        newer.setOrderNumber("200");
        newer.setDeliveryDate("2025-01-01");
        newer.setCreationDate("2025-01-01");

        when(orderService.getAllOrders()).thenReturn(Arrays.asList(older, newer));

        mockMvc.perform(get("/api/index/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber").value("200"))
                .andExpect(jsonPath("$[1].orderNumber").value("100"));
    }

    @Test
    void postRefreshOrdersUsesSemanticMutatingEndpoint() throws Exception {
        List<OrderDto> updatedOrders = Collections.singletonList(new OrderDto());
        when(orderRefreshWorkflowService.refreshOrdersFromCrm()).thenReturn(updatedOrders);

        mockMvc.perform(post("/api/index/refresh/orders"))
                .andExpect(status().isOk());

        verify(orderRefreshWorkflowService).refreshOrdersFromCrm();
        verify(messagingTemplate).convertAndSend(
                ArgumentMatchers.eq("/topic/orderUpdate"),
                ArgumentMatchers.<Object>any()
        );
    }
}
