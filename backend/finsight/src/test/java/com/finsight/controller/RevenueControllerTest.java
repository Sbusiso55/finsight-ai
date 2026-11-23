
package com.finsight.controller;

import com.finsight.security.JwtService;
import com.finsight.service.RevenueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
//import com.finsight.config.AuthenticationFilter;
//import com.finsight.config.SecurityConfig;
import com.finsight.security.CustomUserDetailsService;
//import com.finsight.security.JwtService;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Import;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(RevenueController.class)
class RevenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RevenueService revenueService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private final Long businessId = 1L;
    private final LocalDate from = LocalDate.of(2026, 1, 1);
    private final LocalDate to = LocalDate.of(2026, 1, 31);

    @Test
    void shouldGetTotalRevenue() throws Exception {
        when(revenueService.calculateTotalRevenue(businessId, from, to))
                .thenReturn(new BigDecimal("1000.00"));

        mockMvc.perform(get("/api/businesses/1/revenue/total")
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(content().json("1000.00"));

        verify(revenueService).calculateTotalRevenue(businessId, from, to);
    }

    @Test
    void shouldGetRevenueByCategory() throws Exception {
        when(revenueService.calculateRevenueByCategory(businessId, from, to))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/businesses/1/revenue/by-category")
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(revenueService).calculateRevenueByCategory(businessId, from, to);
    }

    @Test
    void shouldGetRevenueGrowthRate() throws Exception {
        LocalDate previousFrom = from.minusMonths(1);
        LocalDate previousTo = to.minusMonths(1);

        when(revenueService.calculateRevenueGrowthRate(
                businessId, from, to, previousFrom, previousTo))
                .thenReturn(new BigDecimal("20.0000"));

        mockMvc.perform(get("/api/businesses/1/revenue/growth")
                        .param("currentFrom", "2026-01-01")
                        .param("currentTo", "2026-01-31")
                        .param("previousFrom", "2025-12-01")
                        .param("previousTo", "2025-12-31"))
                .andExpect(status().isOk())
                .andExpect(content().json("20.0000"));

        verify(revenueService).calculateRevenueGrowthRate(
                businessId, from, to, previousFrom, previousTo);
    }

    @Test
    void shouldGetGrossProfitMargin() throws Exception {
        when(revenueService.calculateGrossProfitMargin(businessId, from, to))
                .thenReturn(new BigDecimal("40.0000"));

        mockMvc.perform(get("/api/businesses/1/revenue/gross-profit-margin")
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(content().json("40.0000"));

        verify(revenueService).calculateGrossProfitMargin(businessId, from, to);
    }

    @Test
    void shouldGetNetProfitMargin() throws Exception {
        when(revenueService.calculateNetProfitMargin(businessId, from, to))
                .thenReturn(new BigDecimal("25.0000"));

        mockMvc.perform(get("/api/businesses/1/revenue/net-profit-margin")
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(content().json("25.0000"));

        verify(revenueService).calculateNetProfitMargin(businessId, from, to);
    }

    @Test
    void shouldRejectRequestWhenDateParameterIsMissing() throws Exception {
        mockMvc.perform(get("/api/businesses/1/revenue/total")
                        .param("from", "2026-01-01"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(revenueService);
    }
}
