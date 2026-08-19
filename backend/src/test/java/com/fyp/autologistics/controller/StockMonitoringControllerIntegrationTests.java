package com.fyp.autologistics.controller;

import com.fyp.autologistics.dto.StockAlertRuleResponse;
import com.fyp.autologistics.dto.StockMonitoringOverviewResponse;
import com.fyp.autologistics.enums.LogisticsStage;
import com.fyp.autologistics.enums.StockLevelStatus;
import com.fyp.autologistics.enums.UserRole;
import com.fyp.autologistics.service.StockMonitoringService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StockMonitoringControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockMonitoringService stockMonitoringService;

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/stock-monitoring"))
                .andExpect(status().isForbidden());

        verify(stockMonitoringService, never()).getOverview(any());
    }

    @Test
    @WithMockUser(username = "manufacturer@example.com", roles = "MANUFACTURER")
    void manufacturerCanReadScopedOverview() throws Exception {
        when(stockMonitoringService.getOverview("manufacturer@example.com"))
                .thenReturn(new StockMonitoringOverviewResponse(
                        List.of(buildRuleResponse()),
                        List.of(),
                        List.of(),
                        List.of()
                ));

        mockMvc.perform(get("/api/stock-monitoring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rules[0].ruleKey").value("STK-TEST-001"))
                .andExpect(jsonPath("$.rules[0].currentQuantity").value(2));

        verify(stockMonitoringService).getOverview("manufacturer@example.com");
    }

    @Test
    @WithMockUser(username = "manufacturer@example.com", roles = "MANUFACTURER")
    void manufacturerCannotCreateRule() throws Exception {
        mockMvc.perform(post("/api/stock-monitoring/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRuleRequest()))
                .andExpect(status().isForbidden());

        verify(stockMonitoringService, never()).createRule(any(), any());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void adminCanCreateValidRule() throws Exception {
        when(stockMonitoringService.createRule(any(), any()))
                .thenReturn(buildRuleResponse());

        mockMvc.perform(post("/api/stock-monitoring/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRuleRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruleKey").value("STK-TEST-001"));

        verify(stockMonitoringService).createRule(any(),
                org.mockito.ArgumentMatchers.eq("admin@example.com"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void invalidMinimumQuantityReturnsValidationError() throws Exception {
        mockMvc.perform(post("/api/stock-monitoring/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "partyKey": "MFG-TEST",
                                  "vehicleModelCode": "MODEL-TEST",
                                  "logisticsStage": "STOCK_YARD",
                                  "minimumQuantity": 0,
                                  "active": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Minimum quantity must be at least 1."));

        verify(stockMonitoringService, never()).createRule(any(), any());
    }

    private String validRuleRequest() {
        return """
                {
                  "partyKey": "MFG-TEST",
                  "vehicleModelCode": "MODEL-TEST",
                  "logisticsStage": "STOCK_YARD",
                  "minimumQuantity": 3,
                  "active": true
                }
                """;
    }

    private StockAlertRuleResponse buildRuleResponse() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 8, 18, 12, 0);
        return new StockAlertRuleResponse(
                1L,
                "STK-TEST-001",
                "MFG-TEST",
                UserRole.MANUFACTURER,
                "Test Manufacturer",
                "MODEL-TEST",
                "Test Model",
                LogisticsStage.STOCK_YARD,
                3,
                2,
                StockLevelStatus.LOW,
                true,
                timestamp,
                timestamp,
                timestamp
        );
    }
}
