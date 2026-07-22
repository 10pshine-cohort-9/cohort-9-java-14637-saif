package com.saif.contactmanagement.repository;

import com.saif.contactmanagement.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findByUserId(Long userId);


    List<Contact> findByUserIdAndFirstNameContainingIgnoreCaseOrUserIdAndLastNameContainingIgnoreCase(
            Long userId1,
            String firstName,
            Long userId2,
            String lastName
    );

}