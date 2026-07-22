package com.saif.contactmanagement.repository;

import com.saif.contactmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}