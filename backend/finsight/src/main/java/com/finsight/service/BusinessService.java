
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

    public List<Business> getBusinessesByOwner(Long ownerId) {
        return businessRepository.findByOwnerId(ownerId);
    }

    public List<Business> getAccessibleBusinesses(Long userId) {
        return businessRepository.findAccessibleByUser(userId);
    }

    public Optional<Business> getBusinessById(Long id) {
        return businessRepository.findById(id);
    }

    public Business saveBusiness(Business business) {
        return businessRepository.save(business);
    }

    public void deleteBusiness(Long id) {
        businessRepository.deleteById(id);
    }
}

