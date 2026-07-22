package com.saif.contactmanagement.service;

import com.saif.contactmanagement.entity.Contact;

import java.util.List;

public interface ContactService {

    Contact createContact(Contact contact);

    List<Contact> getAllContacts(Long userId);

    Contact getContactById(Long id);

    Contact updateContact(Long id, Contact contact);

    void deleteContact(Long id);

    List<Contact> searchContacts(Long userId, String keyword);
}