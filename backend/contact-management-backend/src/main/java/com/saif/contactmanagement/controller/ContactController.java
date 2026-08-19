package com.saif.contactmanagement.controller;

import com.saif.contactmanagement.dto.request.ContactRequest;
import com.saif.contactmanagement.dto.response.ContactResponse;
import com.saif.contactmanagement.entity.Contact;
import com.saif.contactmanagement.service.ContactService;
import com.saif.contactmanagement.service.impl.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getUser().getId();
        }
        throw new BadCredentialsException("User not authenticated");
    }

    private Contact toEntity(ContactRequest request) {
        return Contact.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .title(request.getTitle())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .company(request.getCompany())
                .address(request.getAddress())
                .notes(request.getNotes())
                .favorite(request.getFavorite() != null ? request.getFavorite() : false)
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
                .build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactResponse createContact(@Valid @RequestBody ContactRequest request) {
        Contact contact = toEntity(request);
        Contact savedContact = contactService.createContact(contact);
        return toResponse(savedContact);
    }

    @GetMapping
    public List<ContactResponse> getAllContacts() {
        Long userId = getCurrentUserId();
        List<Contact> contacts = contactService.getAllContacts(userId);
        return contacts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ContactResponse getContactById(@PathVariable Long id) {
        Contact contact = contactService.getContactById(id);
        return toResponse(contact);
    }

    @PutMapping("/{id}")
    public ContactResponse updateContact(@PathVariable Long id, @Valid @RequestBody ContactRequest request) {
        Contact contactDetails = toEntity(request);
        Contact updatedContact = contactService.updateContact(id, contactDetails);
        return toResponse(updatedContact);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContact(@PathVariable Long id) {
        contactService.deleteContact(id);
    }

    @GetMapping("/search")
    public List<ContactResponse> searchContacts(@RequestParam String keyword) {
        Long userId = getCurrentUserId();
        List<Contact> contacts = contactService.searchContacts(userId, keyword);
        return contacts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
