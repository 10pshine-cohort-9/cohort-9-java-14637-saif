package com.saif.contactmanagement.service.impl;

import com.saif.contactmanagement.entity.Contact;
import com.saif.contactmanagement.repository.ContactRepository;
import com.saif.contactmanagement.service.ContactService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;

    public ContactServiceImpl(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Override
    public Contact createContact(Contact contact) {
        return contactRepository.save(contact);
    }

    @Override
    public List<Contact> getAllContacts(Long userId) {
        return contactRepository.findByUserId(userId);
    }

    @Override
    public Contact getContactById(Long id) {
        return contactRepository.findById(id)
                .orElseThrow();
    }

    @Override
    public Contact updateContact(Long id, Contact contact) {

        Contact existingContact = contactRepository.findById(id)
                .orElseThrow();

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
                .orElseThrow();

        contactRepository.delete(contact);
    }

    @Override
    public List<Contact> searchContacts(Long userId, String keyword) {

        return contactRepository
                .findByUserIdAndFirstNameContainingIgnoreCaseOrUserIdAndLastNameContainingIgnoreCase(
                        userId,
                        keyword,
                        userId,
                        keyword
                );
    }
}