package com.finsight.controller;

import com.finsight.dto.CategoryDTO;
import com.finsight.model.Business;
import com.finsight.model.Category;
import com.finsight.security.BusinessAccess;
import com.finsight.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final BusinessAccess businessAccess;

    public CategoryController(CategoryService categoryService, BusinessAccess businessAccess) {
        this.categoryService = categoryService;
        this.businessAccess = businessAccess;
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<CategoryDTO>> getByBusiness(@PathVariable Long businessId,
                                                           @AuthenticationPrincipal UserDetails user) {
        businessAccess.requireOwned(businessId, user.getUsername());
        return ResponseEntity.ok(
                categoryService.getCategoriesByBusiness(businessId).stream()
                        .map(CategoryDTO::from)
                        .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getById(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails user) {
        Category category = findOwned(id, user);
        return ResponseEntity.ok(CategoryDTO.from(category));
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> create(@RequestBody Category category,
                                              @AuthenticationPrincipal UserDetails user) {
        if (category.getBusiness() == null || category.getBusiness().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Business is required");
        }
        if (category.getName() == null || category.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        Business business = businessAccess.requireOwned(category.getBusiness().getId(), user.getUsername());
        category.setId(null);
        category.setBusiness(business);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CategoryDTO.from(categoryService.saveCategory(category)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> update(@PathVariable Long id,
                                              @RequestBody Category category,
                                              @AuthenticationPrincipal UserDetails user) {
        Category existing = findOwned(id, user);
        existing.setName(category.getName());
        existing.setType(category.getType());
        existing.setCostBehavior(category.getCostBehavior());
        existing.setCostClassification(category.getCostClassification());
        return ResponseEntity.ok(CategoryDTO.from(categoryService.saveCategory(existing)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails user) {
        findOwned(id, user);
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    private Category findOwned(Long id, UserDetails user) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        businessAccess.requireOwned(category.getBusiness().getId(), user.getUsername());
        return category;
    }
}