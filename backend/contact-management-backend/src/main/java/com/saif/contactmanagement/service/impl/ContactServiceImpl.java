package com.saif.contactmanagement.service.impl;

import com.saif.contactmanagement.repository.ContactRepository;
import com.saif.contactmanagement.service.ContactService;
import org.springframework.stereotype.Service;

@Service
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;

    public ContactServiceImpl(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }
}