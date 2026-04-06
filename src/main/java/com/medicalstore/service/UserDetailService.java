package com.medicalstore.service;

import com.medicalstore.model.UserDetails;
import com.medicalstore.repo.UserDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserDetailService {

    private final UserDetailRepository userDetailRepository;
    
    @Autowired
    private PasswordService passwordService;

    public UserDetailService(UserDetailRepository userDetailRepository) {
        this.userDetailRepository = userDetailRepository;
    }

    public boolean register(UserDetails u) {
        if (userDetailRepository.existsByUsername(u.getUsername())) {
            return false;
        }
        u.setPassword(passwordService.encode(u.getPassword()));
        userDetailRepository.save(u);
        return true;
    }

    public UserDetails login(String username, String password) {
        UserDetails u = userDetailRepository.findByUsername(username);
        if (u != null && passwordService.matches(password, u.getPassword())) {
            return u;
        }
        return null;
    }
}
