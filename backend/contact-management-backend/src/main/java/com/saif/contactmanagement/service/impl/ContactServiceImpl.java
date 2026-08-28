package com.saif.contactmanagement.service.impl;

import com.saif.contactmanagement.entity.Contact;
import com.saif.contactmanagement.entity.User;
import com.saif.contactmanagement.exception.ResourceNotFoundException;
import com.saif.contactmanagement.repository.ContactRepository;
import com.saif.contactmanagement.service.ContactService;
import com.saif.contactmanagement.util.CsvHelper;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import com.saif.contactmanagement.security.CurrentUserProvider;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ContactServiceImpl implements ContactService {

    private static final String CONTACT_NOT_FOUND = "Contact not found";

    private final ContactRepository contactRepository;
    private final CurrentUserProvider currentUserProvider;
    private final jakarta.validation.Validator validator;

    public ContactServiceImpl(ContactRepository contactRepository, CurrentUserProvider currentUserProvider, jakarta.validation.Validator validator) {
        this.contactRepository = contactRepository;
        this.currentUserProvider = currentUserProvider;
        this.validator = validator;
    }

    private void validateOwnership(Contact contact) {
        User currentUser = currentUserProvider.getCurrentUser();
        if (contact.getUser() == null || !contact.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException(CONTACT_NOT_FOUND);
        }
    }

    @Override
    public Contact createContact(Contact contact) {
        contact.setUser(currentUserProvider.getCurrentUser());
        return contactRepository.save(contact);
    }

    @Override
    public Page<Contact> getAllContacts(Pageable pageable) {
        User currentUser = currentUserProvider.getCurrentUser();
        return contactRepository.findByUserId(currentUser.getId(), pageable);
    }

    @Override
    public Contact getContactById(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CONTACT_NOT_FOUND));
        validateOwnership(contact);
        return contact;
    }

    @Override
    @Transactional
    public Contact updateContact(Long id, Contact contact) {
        Contact existingContact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CONTACT_NOT_FOUND));
        validateOwnership(existingContact);

        existingContact.setFirstName(contact.getFirstName());
        existingContact.setLastName(contact.getLastName());
        existingContact.setTitle(contact.getTitle());
        existingContact.setEmail(contact.getEmail());
        existingContact.setPhoneNumber(contact.getPhoneNumber());
        existingContact.setCompany(contact.getCompany());
        existingContact.setAddress(contact.getAddress());
        existingContact.setNotes(contact.getNotes());
        existingContact.setFavorite(contact.getFavorite());

        existingContact.getEmails().clear();
        if (contact.getEmails() != null) {
            existingContact.getEmails().putAll(contact.getEmails());
        }
        existingContact.getPhoneNumbers().clear();
        if (contact.getPhoneNumbers() != null) {
            existingContact.getPhoneNumbers().putAll(contact.getPhoneNumbers());
        }

        return contactRepository.save(existingContact);
    }

    @Override
    @Transactional
    public void deleteContact(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CONTACT_NOT_FOUND));
        validateOwnership(contact);
        contactRepository.delete(contact);
    }

    @Override
    public Page<Contact> searchContacts(String keyword, Pageable pageable) {
        User currentUser = currentUserProvider.getCurrentUser();
        return contactRepository.searchByUserIdAndKeyword(currentUser.getId(), keyword, pageable);
    }

    @Override
    public String exportContactsToCsv() {
        User currentUser = currentUserProvider.getCurrentUser();
        List<Contact> contacts = contactRepository.findByUserId(currentUser.getId());
        return CsvHelper.contactsToCsv(contacts);
    }

    @Override
    @Transactional
    public int importContactsFromCsv(String csvContent) {
        User currentUser = currentUserProvider.getCurrentUser();
        List<Contact> contacts = CsvHelper.csvToContacts(csvContent);
        if (contacts.isEmpty()) {
            return 0;
        }

        for (Contact contact : contacts) {
            validateImportedContact(contact);
            contact.setUser(currentUser);
        }

        contactRepository.saveAll(contacts);
        return contacts.size();
    }

    private void validateImportedContact(Contact contact) {
        com.saif.contactmanagement.dto.request.ContactRequest contactRequest = new com.saif.contactmanagement.dto.request.ContactRequest();
        contactRequest.setFirstName(contact.getFirstName());
        contactRequest.setLastName(contact.getLastName());
        contactRequest.setTitle(contact.getTitle());
        contactRequest.setEmail(contact.getEmail());
        contactRequest.setPhoneNumber(contact.getPhoneNumber());
        contactRequest.setCompany(contact.getCompany());
        contactRequest.setAddress(contact.getAddress());
        contactRequest.setNotes(contact.getNotes());
        contactRequest.setFavorite(contact.getFavorite());
        contactRequest.setEmails(contact.getEmails());
        contactRequest.setPhoneNumbers(contact.getPhoneNumbers());

        java.util.Set<jakarta.validation.ConstraintViolation<com.saif.contactmanagement.dto.request.ContactRequest>> violations = validator.validate(contactRequest);
        if (!violations.isEmpty()) {
            String message = violations.iterator().next().getMessage();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}