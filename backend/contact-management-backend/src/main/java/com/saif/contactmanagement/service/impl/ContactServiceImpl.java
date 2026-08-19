package com.saif.contactmanagement.service.impl;

import com.saif.contactmanagement.entity.Contact;
import com.saif.contactmanagement.entity.User;
import com.saif.contactmanagement.exception.ResourceNotFoundException;
import com.saif.contactmanagement.repository.ContactRepository;
import com.saif.contactmanagement.service.ContactService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;

    public ContactServiceImpl(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("User not authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser();
        }
        throw new BadCredentialsException("Invalid user details");
    }

    private void validateOwnership(Contact contact) {
        User currentUser = getCurrentUser();
        if (contact.getUser() == null || !contact.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Contact not found");
        }
    }

    @Override
    public Contact createContact(Contact contact) {
        contact.setUser(getCurrentUser());
        return contactRepository.save(contact);
    }

    @Override
    public List<Contact> getAllContacts(Long userId) {
        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(userId)) {
            throw new ResourceNotFoundException("Contact not found");
        }
        return contactRepository.findByUserId(userId);
    }

    @Override
    public Contact getContactById(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));
        validateOwnership(contact);
        return contact;
    }

    @Override
    public Contact updateContact(Long id, Contact contact) {
        Contact existingContact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));
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

        return contactRepository.save(existingContact);
    }

    @Override
    public void deleteContact(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));
        validateOwnership(contact);
        contactRepository.delete(contact);
    }

    @Override
    public List<Contact> searchContacts(Long userId, String keyword) {
        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(userId)) {
            throw new ResourceNotFoundException("Contact not found");
        }
        return contactRepository
                .findByUserIdAndFirstNameContainingIgnoreCaseOrUserIdAndLastNameContainingIgnoreCase(
                        userId,
                        keyword,
                        userId,
                        keyword
                );
    }
}