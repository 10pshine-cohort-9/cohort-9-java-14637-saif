package com.saif.contactmanagement.controller;

import com.saif.contactmanagement.dto.request.ContactRequest;
import com.saif.contactmanagement.dto.response.ContactResponse;
import com.saif.contactmanagement.entity.Contact;
import com.saif.contactmanagement.service.ContactService;
import com.saif.contactmanagement.service.impl.CustomUserDetails;
import com.saif.contactmanagement.security.CurrentUserProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;
    private final CurrentUserProvider currentUserProvider;

    public ContactController(ContactService contactService, CurrentUserProvider currentUserProvider) {
        this.contactService = contactService;
        this.currentUserProvider = currentUserProvider;
    }

    private Contact toEntity(ContactRequest request) {
        Map<String, String> requestEmails = request.getEmails();
        if (requestEmails == null) {
            requestEmails = new HashMap<>();
        }
        if (request.getEmail() != null && !request.getEmail().isBlank() && !requestEmails.containsValue(request.getEmail())) {
            requestEmails.put("primary", request.getEmail());
        }

        Map<String, String> requestPhoneNumbers = request.getPhoneNumbers();
        if (requestPhoneNumbers == null) {
            requestPhoneNumbers = new HashMap<>();
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank() && !requestPhoneNumbers.containsValue(request.getPhoneNumber())) {
            requestPhoneNumbers.put("primary", request.getPhoneNumber());
        }

        return Contact.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .title(request.getTitle())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .company(request.getCompany())
                .address(request.getAddress())
                .notes(request.getNotes())
                .favorite(Boolean.TRUE.equals(request.getFavorite()))
                .emails(requestEmails)
                .phoneNumbers(requestPhoneNumbers)
                .build();
    }

    private ContactResponse toResponse(Contact contact) {
        return ContactResponse.builder()
                .id(contact.getId())
                .firstName(contact.getFirstName())
                .lastName(contact.getLastName())
                .title(contact.getTitle())
                .email(contact.getEmail())
                .phoneNumber(contact.getPhoneNumber())
                .company(contact.getCompany())
                .address(contact.getAddress())
                .notes(contact.getNotes())
                .favorite(contact.getFavorite())
                .userId(contact.getUser() != null ? contact.getUser().getId() : null)
                .emails(contact.getEmails())
                .phoneNumbers(contact.getPhoneNumbers())
                .build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactResponse createContact(@Valid @RequestBody ContactRequest request) {
        log.info("Creating contact: {} {}", request.getFirstName(), request.getLastName());
        Contact contact = toEntity(request);
        Contact savedContact = contactService.createContact(contact);
        return toResponse(savedContact);
    }

    @GetMapping
    public Page<ContactResponse> getAllContacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Long userId = currentUserProvider.getCurrentUserId();
        int limitSize = Math.min(size, 10);
        log.info("Fetching contacts for user ID: {}, page: {}, size: {}", userId, page, limitSize);
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, limitSize, sort);
        Page<Contact> contactsPage = contactService.getAllContacts(pageable);
        return contactsPage.map(this::toResponse);
    }

    @GetMapping("/{id}")
    public ContactResponse getContactById(@PathVariable Long id) {
        log.info("Fetching contact ID: {}", id);
        Contact contact = contactService.getContactById(id);
        return toResponse(contact);
    }

    @PutMapping("/{id}")
    public ContactResponse updateContact(@PathVariable Long id, @Valid @RequestBody ContactRequest request) {
        log.info("Updating contact ID: {}", id);
        Contact contactDetails = toEntity(request);
        Contact updatedContact = contactService.updateContact(id, contactDetails);
        return toResponse(updatedContact);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContact(@PathVariable Long id) {
        log.info("Deleting contact ID: {}", id);
        contactService.deleteContact(id);
    }

    @GetMapping("/search")
    public Page<ContactResponse> searchContacts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Long userId = currentUserProvider.getCurrentUserId();
        int limitSize = Math.min(size, 10);
        log.info("Searching contacts with keyword '{}' for user ID: {}, page: {}, size: {}", keyword, userId, page, limitSize);
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, limitSize, sort);
        Page<Contact> contactsPage = contactService.searchContacts(keyword, pageable);
        return contactsPage.map(this::toResponse);
    }
}
