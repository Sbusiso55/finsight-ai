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

    public List<Business> findAll() {
        return businessRepository.findAll();
    }

    public Optional<Business> findById(Long id) {
        return businessRepository.findById(id);
    }

    public List<Business> findByUserId(Long userId) {
        return businessRepository.findByUserId(userId);
    }

    public Business save(Business business) {
        return businessRepository.save(business);
    }

    public void deleteById(Long id) {
        businessRepository.deleteById(id);
    }
}
