package com.finsight.service;

import com.finsight.model.Business;
import com.finsight.repository.BusinessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class BusinessService {

    @Autowired
    private BusinessRepository businessRepository;

    public Business createBusiness(Business business) {
        return businessRepository.save(business);
    }

    public Optional<Business> getBusinessById(Long id) {
        return businessRepository.findById(id);
    }

    public List<Business> getBusinessesByUserId(Long userId) {
        return businessRepository.findByUserId(userId);
    }

    public Business updateBusiness(Long id, Business updatedBusiness) {
        return businessRepository.findById(id)
                .map(existing -> {
                    existing.setName(updatedBusiness.getName());
                    existing.setIndustry(updatedBusiness.getIndustry());
                    existing.setDescription(updatedBusiness.getDescription());
                    return businessRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Business not found with id: " + id));
    }

    public void deleteBusiness(Long id) {
        businessRepository.deleteById(id);
    }
}

