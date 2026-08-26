package com.saif.contactmanagement.service;

import com.saif.contactmanagement.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContactService {

    Contact createContact(Contact contact);

    Page<Contact> getAllContacts(Pageable pageable);

    Contact getContactById(Long id);

    Contact updateContact(Long id, Contact contact);

    void deleteContact(Long id);

    Page<Contact> searchContacts(String keyword, Pageable pageable);

    String exportContactsToCsv();

    int importContactsFromCsv(String csvContent);
}