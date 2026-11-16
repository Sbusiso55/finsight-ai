package com.finsight.controller;

import com.finsight.dto.BusinessDTO;
import com.finsight.dto.BusinessRequest;
import com.finsight.model.Business;
import com.finsight.model.Category;
import com.finsight.model.enums.CostBehavior;
import com.finsight.model.enums.CostClassification;
import com.finsight.model.enums.TransactionType;
import com.finsight.security.BusinessAccess;
import com.finsight.service.BusinessService;
import com.finsight.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/businesses")
public class BusinessController {

    private final BusinessService businessService;
    private final BusinessAccess businessAccess;
    private final CategoryService categoryService;

    public BusinessController(BusinessService businessService,
                              BusinessAccess businessAccess,
                              CategoryService categoryService) {
        this.businessService = businessService;
        this.businessAccess = businessAccess;
        this.categoryService = categoryService;
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BusinessDTO>> getMyBusinesses(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(
                businessService.getBusinessesByOwnerEmail(user.getUsername()).stream()
                        .map(BusinessDTO::from)
                        .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessDTO> getById(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(BusinessDTO.from(businessAccess.requireOwned(id, user.getUsername())));
    }

    @PostMapping
    public ResponseEntity<BusinessDTO> create(@RequestBody BusinessRequest request,
                                              @AuthenticationPrincipal UserDetails user) {
        validate(request);
        Business business = new Business();
        apply(business, request);
        Business saved = businessService.saveBusiness(business, user.getUsername());
        createDefaultCategories(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(BusinessDTO.from(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BusinessDTO> update(@PathVariable Long id,
                                              @RequestBody BusinessRequest request,
                                              @AuthenticationPrincipal UserDetails user) {
        validate(request);
        Business existing = businessAccess.requireOwned(id, user.getUsername());
        apply(existing, request);
        return ResponseEntity.ok(BusinessDTO.from(businessService.saveBusiness(existing, user.getUsername())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails user) {
        businessAccess.requireOwned(id, user.getUsername());
        businessService.deleteBusiness(id);
        return ResponseEntity.noContent().build();
    }

    private void validate(BusinessRequest r) {
        if (r.getName() == null || r.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Business name is required");
        }
        if (r.getOpeningBalance() != null && r.getOpeningBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Opening balance cannot be negative");
        }
    }

    private void apply(Business b, BusinessRequest r) {
        b.setName(r.getName().trim());
        b.setIndustry(r.getIndustry());
        b.setCurrency(r.getCurrency() == null || r.getCurrency().isBlank() ? "ZAR" : r.getCurrency());
        b.setOpeningBalance(r.getOpeningBalance() == null ? BigDecimal.ZERO : r.getOpeningBalance());
        b.setOpeningBalanceDate(r.getOpeningBalanceDate());
        b.setSeasonal(r.isSeasonal());
    }

    private void createDefaultCategories(Business business) {
        addCategory(business, "Sales", TransactionType.INCOME, null, null);
        addCategory(business, "Services", TransactionType.INCOME, null, null);
        addCategory(business, "Other Income", TransactionType.INCOME, null, null);

        addCategory(business, "Stock Purchases", TransactionType.EXPENSE,
                CostBehavior.VARIABLE, CostClassification.COST_OF_GOODS_SOLD);
        addCategory(business, "Rent", TransactionType.EXPENSE,
                CostBehavior.FIXED, CostClassification.OPERATING_EXPENSE);
        addCategory(business, "Wages", TransactionType.EXPENSE,
                CostBehavior.FIXED, CostClassification.OPERATING_EXPENSE);
        addCategory(business, "Utilities", TransactionType.EXPENSE,
                CostBehavior.VARIABLE, CostClassification.OPERATING_EXPENSE);
        addCategory(business, "Transport", TransactionType.EXPENSE,
                CostBehavior.VARIABLE, CostClassification.OPERATING_EXPENSE);
        addCategory(business, "Marketing", TransactionType.EXPENSE,
                CostBehavior.VARIABLE, CostClassification.OPERATING_EXPENSE);
        addCategory(business, "Other Expenses", TransactionType.EXPENSE,
                CostBehavior.VARIABLE, CostClassification.OPERATING_EXPENSE);
    }

    private void addCategory(Business business, String name, TransactionType type,
                             CostBehavior behavior, CostClassification classification) {
        Category c = new Category();
        c.setBusiness(business);
        c.setName(name);
        c.setType(type);
        c.setCostBehavior(behavior);
        c.setCostClassification(classification);
        categoryService.saveCategory(c);
    }
}