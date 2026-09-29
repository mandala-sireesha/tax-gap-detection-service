package com.taxgap.taxgapdetection.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.taxgap.taxgapdetection.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

}