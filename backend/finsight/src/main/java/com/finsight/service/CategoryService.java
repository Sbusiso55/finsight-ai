
package com.finsight.service;

import com.finsight.model.Category;
import com.finsight.model.enums.TransactionType;
import com.finsight.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getCategoriesByBusiness(Long businessId) {
        return categoryRepository.findByBusinessIdOrderByName(businessId);
    }

    public List<Category> getCategoriesByBusinessAndType(Long businessId, TransactionType type) {
        return categoryRepository.findByBusinessIdAndType(businessId, type);
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    public boolean categoryExists(Long businessId, String name) {
        return categoryRepository.existsByBusinessIdAndNameIgnoreCase(businessId, name);
    }
}

