
package com.finsight.controller;

import com.finsight.dto.CategoryDTO;
import com.finsight.model.Category;
import com.finsight.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<CategoryDTO>> getByBusiness(@PathVariable Long businessId) {
        return ResponseEntity.ok(
                categoryService.getCategoriesByBusiness(businessId).stream()
                        .map(CategoryDTO::from)
                        .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getById(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
                .map(CategoryDTO::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> create(@RequestBody Category category) {
        return ResponseEntity.ok(CategoryDTO.from(categoryService.saveCategory(category)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> update(@PathVariable Long id, @RequestBody Category category) {
        return categoryService.getCategoryById(id)
                .map(existing -> {
                    category.setId(id);
                    return ResponseEntity.ok(CategoryDTO.from(categoryService.saveCategory(category)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}

