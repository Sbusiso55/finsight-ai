package com.finsight.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.finsight.model.Business;
import com.finsight.model.Category;
import com.finsight.model.Transaction;
import com.finsight.model.enums.TransactionType;
import com.finsight.security.BusinessAccess;
import com.finsight.service.CategoryService;
import com.finsight.service.TransactionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.finsight.security.JwtService;
import com.finsight.security.CustomUserDetailsService;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.test.context.support.WithMockUser;


@Import(TransactionControllerTest.TestConfig.class)
@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private BusinessAccess businessAccess;
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Business business;
    private Category category;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        business = new Business();
        business.setId(1L);

        category = new Category();
        category.setId(10L);
        category.setName("Sales");
        category.setBusiness(business);

        transaction = new Transaction();
        transaction.setId(100L);
        transaction.setBusiness(business);
        transaction.setCategory(category);
        transaction.setAmount(new BigDecimal("500.00"));
        transaction.setType(TransactionType.INCOME);
        transaction.setTransactionDate(LocalDate.of(2026, 11, 20));
        transaction.setDescription("Product sale");

        when(businessAccess.requireOwned(anyLong(), anyString()))
                .thenReturn(business);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "OWNER")
    void shouldGetTransactions() throws Exception {

        Page<Transaction> transactions = new PageImpl<>(
                List.of(transaction),
                PageRequest.of(0, 10),
                1
        );

        when(transactionService.findByBusinessId(eq(1L), any()))
                .thenReturn(transactions);

        mockMvc.perform(get("/api/transactions")
                        .param("businessId", "1")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(
                                org.springframework.security.core.userdetails.User
                                        .withUsername("test@example.com")
                                        .password("password")
                                        .roles("OWNER")
                                        .build()
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(100))
                .andExpect(jsonPath("$.content[0].description").value("Product sale"));

        verify(businessAccess).requireOwned(1L, "test@example.com");
        verify(transactionService).findByBusinessId(eq(1L), any());
    }

    @TestConfiguration
    static class TestConfig implements WebMvcConfigurer {

        @Override
        public void addArgumentResolvers(
                List<HandlerMethodArgumentResolver> resolvers) {

            resolvers.add(new AuthenticationPrincipalArgumentResolver());
        }
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "OWNER")
    void shouldGetTransactionById() throws Exception {

        when(transactionService.findByIdAndBusinessId(100L, 1L))
                .thenReturn(Optional.of(transaction));

        mockMvc.perform(get("/api/transactions/100")
                        .param("businessId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.description").value("Product sale"));

        verify(businessAccess).requireOwned(1L, "test@example.com");
        verify(transactionService).findByIdAndBusinessId(100L, 1L);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "OWNER")
    void shouldReturnNotFoundWhenTransactionDoesNotExist() throws Exception {

        when(transactionService.findByIdAndBusinessId(999L, 1L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/transactions/999")
                        .param("businessId", "1"))
                .andExpect(status().isNotFound());

        verify(businessAccess).requireOwned(1L, "test@example.com");
        verify(transactionService).findByIdAndBusinessId(999L, 1L);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "OWNER")
    void shouldCreateTransaction() throws Exception {

        when(categoryService.getCategoryById(10L))
                .thenReturn(Optional.of(category));

        when(transactionService.save(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction saved = invocation.getArgument(0);
                    saved.setId(100L);
                    return saved;
                });

        mockMvc.perform(post("/api/transactions")
                        .contentType("application/json")
                        .content("""
                            {
                                "businessId": 1,
                                "categoryId": 10,
                                "transactionDate": "2026-11-20",
                                "amount": 500.00,
                                "type": "INCOME",
                                "description": "Product sale"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.description").value("Product sale"));

        verify(businessAccess).requireOwned(1L, "test@example.com");
        verify(categoryService).getCategoryById(10L);
        verify(transactionService).save(any(Transaction.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "OWNER")
    void shouldRejectTransactionWithInvalidAmount() throws Exception {

        mockMvc.perform(post("/api/transactions")
                        .contentType("application/json")
                        .content("""
                            {
                                "businessId": 1,
                                "categoryId": 10,
                                "transactionDate": "2026-11-20",
                                "amount": 0,
                                "type": "INCOME",
                                "description": "Invalid transaction"
                            }
                            """))
                .andExpect(status().isBadRequest());

        verify(businessAccess).requireOwned(1L, "test@example.com");
        verify(transactionService, never()).save(any(Transaction.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "OWNER")
    void shouldRejectTransactionWhenRequiredFieldsAreMissing() throws Exception {

        mockMvc.perform(post("/api/transactions")
                        .contentType("application/json")
                        .content("""
                            {
                                "businessId": 1,
                                "categoryId": 10,
                                "amount": 500.00,
                                "description": "Missing type and date"
                            }
                            """))
                .andExpect(status().isBadRequest());

        verify(businessAccess).requireOwned(1L, "test@example.com");
        verify(categoryService, never()).getCategoryById(anyLong());
        verify(transactionService, never()).save(any(Transaction.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "OWNER")
    void shouldRejectTransactionWithInvalidCategory() throws Exception {

        when(categoryService.getCategoryById(999L))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/transactions")
                        .contentType("application/json")
                        .content("""
                            {
                                "businessId": 1,
                                "categoryId": 999,
                                "transactionDate": "2026-11-20",
                                "amount": 500.00,
                                "type": "INCOME",
                                "description": "Invalid category"
                            }
                            """))
                .andExpect(status().isBadRequest());

        verify(categoryService).getCategoryById(999L);
        verify(transactionService, never()).save(any(Transaction.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "OWNER")
    void shouldUpdateTransaction() throws Exception {

        when(transactionService.findByIdAndBusinessId(100L, 1L))
                .thenReturn(Optional.of(transaction));

        when(categoryService.getCategoryById(10L))
                .thenReturn(Optional.of(category));

        when(transactionService.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/transactions/100?businessId=1")
                        .contentType("application/json")
                        .content("""
                            {
                                "businessId": 1,
                                "categoryId": 10,
                                "transactionDate": "2026-11-21",
                                "amount": 750.00,
                                "type": "INCOME",
                                "description": "Updated product sale"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(750.00))
                .andExpect(jsonPath("$.description").value("Updated product sale"));

        verify(transactionService).save(any(Transaction.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "OWNER")
    void shouldReturnNotFoundWhenUpdatingNonExistentTransaction() throws Exception {

        when(transactionService.findByIdAndBusinessId(999L, 1L))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/transactions/999?businessId=1")
                        .contentType("application/json")
                        .content("""
                            {
                                "businessId": 1,
                                "categoryId": 10,
                                "transactionDate": "2026-11-21",
                                "amount": 750.00,
                                "type": "INCOME",
                                "description": "Updated product sale"
                            }
                            """))
                .andExpect(status().isNotFound());

        verify(transactionService, never()).save(any(Transaction.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "OWNER")
    void shouldDeleteTransaction() throws Exception {

        when(transactionService.findByIdAndBusinessId(100L, 1L))
                .thenReturn(Optional.of(transaction));

        mockMvc.perform(delete("/api/transactions/100?businessId=1"))
                .andExpect(status().isNoContent());

        verify(transactionService).deleteById(100L);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "OWNER")
    void shouldReturnNotFoundWhenDeletingNonExistentTransaction() throws Exception {

        when(transactionService.findByIdAndBusinessId(999L, 1L))
                .thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/transactions/999?businessId=1"))
                .andExpect(status().isNotFound());

        verify(transactionService, never()).deleteById(anyLong());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "OWNER")
    void shouldGetRevenue() throws Exception {

        when(transactionService.getTotalRevenue(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("15000.00"));

        mockMvc.perform(get("/api/transactions/revenue")
                        .param("businessId", "1")
                        .param("from", "2026-11-01")
                        .param("to", "2026-11-22"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(15000.00));

        verify(transactionService).getTotalRevenue(
                eq(1L), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "OWNER")
    void shouldGetExpenses() throws Exception {

        when(transactionService.getTotalExpenses(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("6500.00"));

        mockMvc.perform(get("/api/transactions/expenses")
                        .param("businessId", "1")
                        .param("from", "2026-11-01")
                        .param("to", "2026-11-22"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(6500.00));

        verify(transactionService).getTotalExpenses(
                eq(1L), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "OWNER")
    void shouldSortTransactionsByCreatedAtAndIdDescendingByDefault() throws Exception {

        when(transactionService.findByBusinessId(
                eq(1L), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/transactions")
                        .param("businessId", "1"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(transactionService).findByBusinessId(
                eq(1L), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(Sort.Direction.DESC,
                pageable.getSort().getOrderFor("createdAt").getDirection());

        assertEquals(Sort.Direction.DESC,
                pageable.getSort().getOrderFor("id").getDirection());
    }


    @Test
    @WithMockUser(username = "test@example.com", roles = "OWNER")
    void shouldRejectTransactionWhenCategoryBelongsToAnotherBusiness() throws Exception {

        Business anotherBusiness = new Business();
        anotherBusiness.setId(2L);

        Category anotherBusinessCategory = new Category();
        anotherBusinessCategory.setId(20L);
        anotherBusinessCategory.setName("Sales");
        anotherBusinessCategory.setBusiness(anotherBusiness);

        when(categoryService.getCategoryById(20L))
                .thenReturn(Optional.of(anotherBusinessCategory));

        mockMvc.perform(post("/api/transactions")
                        .contentType("application/json")
                        .content("""
                        {
                            "businessId": 1,
                            "categoryId": 20,
                            "transactionDate": "2026-11-20",
                            "amount": 500.00,
                            "type": "INCOME",
                            "description": "Transaction with another business category"
                        }
                        """))
                .andExpect(status().isBadRequest());

        verify(businessAccess).requireOwned(1L, "test@example.com");
        verify(categoryService).getCategoryById(20L);
        verify(transactionService, never()).save(any(Transaction.class));
    }

}
