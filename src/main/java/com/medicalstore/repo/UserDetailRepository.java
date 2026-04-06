package com.medicalstore.repo;


import com.medicalstore.model.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDetailRepository extends JpaRepository<UserDetails, Long> {
    UserDetails findByUsername(String username);
    boolean existsByUsername(String username);
}
