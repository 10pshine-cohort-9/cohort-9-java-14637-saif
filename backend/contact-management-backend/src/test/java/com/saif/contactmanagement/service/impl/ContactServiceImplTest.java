package com.saif.contactmanagement.service.impl;

import com.saif.contactmanagement.entity.Contact;
import com.saif.contactmanagement.entity.User;
import com.saif.contactmanagement.exception.ResourceNotFoundException;
import com.saif.contactmanagement.repository.ContactRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private ContactServiceImpl contactService;

    private User currentUser;
    private User otherUser;
    private Contact contact;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(1L)
                .email("user1@example.com")
                .firstName("User")
                .lastName("One")
                .build();

        otherUser = User.builder()
                .id(2L)
                .email("user2@example.com")
                .firstName("User")
                .lastName("Two")
                .build();

        contact = Contact.builder()
                .id(100L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .user(currentUser)
                .favorite(false)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(User user) {
        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUser()).thenReturn(user);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.isAuthenticated()).thenReturn(true);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockUnauthenticatedSecurityContext() {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockInvalidPrincipalSecurityContext() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("invalid-principal");
        when(authentication.isAuthenticated()).thenReturn(true);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // --- Create Contact Tests ---

    @Test
    void shouldCreateContactSuccessfully() {
        mockSecurityContext(currentUser);
        when(contactRepository.save(any(Contact.class))).thenReturn(contact);

        Contact inputContact = Contact.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

        Contact result = contactService.createContact(inputContact);

        assertNotNull(result);
        assertEquals(currentUser, inputContact.getUser());
        verify(contactRepository).save(inputContact);
    }

    @Test
    void shouldThrowBadCredentialsWhenCreatingContactWithoutAuth() {
        mockUnauthenticatedSecurityContext();
        Contact inputContact = Contact.builder().firstName("John").build();

        assertThrows(BadCredentialsException.class, () -> contactService.createContact(inputContact));
        verify(contactRepository, never()).save(any(Contact.class));
    }

    @Test
    void shouldThrowBadCredentialsWhenCreatingContactWithInvalidPrincipal() {
        mockInvalidPrincipalSecurityContext();
        Contact inputContact = Contact.builder().firstName("John").build();

        assertThrows(BadCredentialsException.class, () -> contactService.createContact(inputContact));
        verify(contactRepository, never()).save(any(Contact.class));
    }

    // --- Get All Contacts Tests ---

    @Test
    void shouldGetAllContactsSuccessfully() {
        mockSecurityContext(currentUser);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contact> contactPage = new PageImpl<>(Collections.singletonList(contact));
        when(contactRepository.findByUserId(1L, pageable)).thenReturn(contactPage);

        Page<Contact> result = contactService.getAllContacts(1L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(contact, result.getContent().get(0));
        verify(contactRepository).findByUserId(1L, pageable);
    }

    @Test
    void shouldThrowResourceNotFoundWhenGettingAllContactsOfOtherUser() {
        mockSecurityContext(currentUser);
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(ResourceNotFoundException.class, () -> contactService.getAllContacts(2L, pageable));
        verify(contactRepository, never()).findByUserId(anyLong(), any(Pageable.class));
    }

    // --- Get Contact By ID Tests ---

    @Test
    void shouldGetContactByIdSuccessfully() {
        mockSecurityContext(currentUser);
        when(contactRepository.findById(100L)).thenReturn(Optional.of(contact));

        Contact result = contactService.getContactById(100L);

        assertNotNull(result);
        assertEquals(contact, result);
    }

    @Test
    void shouldThrowResourceNotFoundWhenContactDoesNotExist() {
        when(contactRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> contactService.getContactById(999L));
    }

    @Test
    void shouldThrowResourceNotFoundWhenContactBelongsToOtherUser() {
        mockSecurityContext(currentUser);
        Contact otherContact = Contact.builder()
                .id(200L)
                .user(otherUser)
                .build();
        when(contactRepository.findById(200L)).thenReturn(Optional.of(otherContact));

        assertThrows(ResourceNotFoundException.class, () -> contactService.getContactById(200L));
    }

    // --- Update Contact Tests ---

    @Test
    void shouldUpdateContactSuccessfully() {
        mockSecurityContext(currentUser);
        when(contactRepository.findById(100L)).thenReturn(Optional.of(contact));
        when(contactRepository.save(any(Contact.class))).thenReturn(contact);

        Contact updatedInfo = Contact.builder()
                .firstName("Jane")
                .lastName("Smith")
                .title("Dr.")
                .email("jane@example.com")
                .phoneNumber("+1234567890")
                .company("Company Inc")
                .address("123 Main St")
                .notes("Notes updated")
                .favorite(true)
                .build();

        Contact result = contactService.updateContact(100L, updatedInfo);

        assertNotNull(result);
        assertEquals("Jane", contact.getFirstName());
        assertEquals("Smith", contact.getLastName());
        assertEquals("Dr.", contact.getTitle());
        assertEquals("jane@example.com", contact.getEmail());
        assertEquals("+1234567890", contact.getPhoneNumber());
        assertEquals("Company Inc", contact.getCompany());
        assertEquals("123 Main St", contact.getAddress());
        assertEquals("Notes updated", contact.getNotes());
        assertTrue(contact.getFavorite());
    }

    @Test
    void shouldThrowResourceNotFoundWhenUpdatingNonExistentContact() {
        when(contactRepository.findById(999L)).thenReturn(Optional.empty());

        Contact updatedInfo = Contact.builder().firstName("Jane").build();

        assertThrows(ResourceNotFoundException.class, () -> contactService.updateContact(999L, updatedInfo));
    }

    @Test
    void shouldThrowResourceNotFoundWhenUpdatingContactOfOtherUser() {
        mockSecurityContext(currentUser);
        Contact otherContact = Contact.builder()
                .id(200L)
                .user(otherUser)
                .build();
        when(contactRepository.findById(200L)).thenReturn(Optional.of(otherContact));

        Contact updatedInfo = Contact.builder().firstName("Jane").build();

        assertThrows(ResourceNotFoundException.class, () -> contactService.updateContact(200L, updatedInfo));
    }

    // --- Delete Contact Tests ---

    @Test
    void shouldDeleteContactSuccessfully() {
        mockSecurityContext(currentUser);
        when(contactRepository.findById(100L)).thenReturn(Optional.of(contact));

        assertDoesNotThrow(() -> contactService.deleteContact(100L));
        verify(contactRepository).delete(contact);
    }

    @Test
    void shouldThrowResourceNotFoundWhenDeletingNonExistentContact() {
        when(contactRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> contactService.deleteContact(999L));
        verify(contactRepository, never()).delete(any(Contact.class));
    }

    @Test
    void shouldThrowResourceNotFoundWhenDeletingContactOfOtherUser() {
        mockSecurityContext(currentUser);
        Contact otherContact = Contact.builder()
                .id(200L)
                .user(otherUser)
                .build();
        when(contactRepository.findById(200L)).thenReturn(Optional.of(otherContact));

        assertThrows(ResourceNotFoundException.class, () -> contactService.deleteContact(200L));
        verify(contactRepository, never()).delete(any(Contact.class));
    }

    // --- Search Contacts Tests ---

    @Test
    void shouldSearchContactsSuccessfully() {
        mockSecurityContext(currentUser);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contact> contactPage = new PageImpl<>(Collections.singletonList(contact));
        when(contactRepository.findByUserIdAndFirstNameContainingIgnoreCaseOrUserIdAndLastNameContainingIgnoreCase(
                1L, "keyword", 1L, "keyword", pageable))
                .thenReturn(contactPage);

        Page<Contact> result = contactService.searchContacts(1L, "keyword", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(contact, result.getContent().get(0));
    }

    @Test
    void shouldThrowResourceNotFoundWhenSearchingOtherUserContacts() {
        mockSecurityContext(currentUser);
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(ResourceNotFoundException.class, () -> contactService.searchContacts(2L, "keyword", pageable));
        verify(contactRepository, never()).findByUserIdAndFirstNameContainingIgnoreCaseOrUserIdAndLastNameContainingIgnoreCase(
                anyLong(), anyString(), anyLong(), anyString(), any(Pageable.class));
    }

    @Test
    void shouldThrowResourceNotFoundWhenContactHasNoUser() {
        mockSecurityContext(currentUser);
        Contact contactNoUser = Contact.builder().id(300L).user(null).build();
        when(contactRepository.findById(300L)).thenReturn(Optional.of(contactNoUser));

        assertThrows(ResourceNotFoundException.class, () -> contactService.getContactById(300L));
    }

    @Test
    void shouldThrowBadCredentialsWhenAuthenticationIsNotAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Contact inputContact = Contact.builder().firstName("John").build();
        assertThrows(BadCredentialsException.class, () -> contactService.createContact(inputContact));
    }

    @Test
    void shouldUpdateContactWithEmailsAndPhoneNumbersMapsSuccessfully() {
        mockSecurityContext(currentUser);
        when(contactRepository.findById(100L)).thenReturn(Optional.of(contact));
        when(contactRepository.save(any(Contact.class))).thenReturn(contact);

        Map<String, String> emails = new HashMap<>();
        emails.put("work", "work@example.com");
        emails.put("personal", "personal@example.com");

        Map<String, String> phoneNumbers = new HashMap<>();
        phoneNumbers.put("work", "+111222333");
        phoneNumbers.put("home", "+444555666");

        Contact updatedInfo = Contact.builder()
                .firstName("Jane")
                .lastName("Smith")
                .emails(emails)
                .phoneNumbers(phoneNumbers)
                .build();

        Contact result = contactService.updateContact(100L, updatedInfo);

        assertNotNull(result);
        assertEquals("Jane", contact.getFirstName());
        assertEquals("Smith", contact.getLastName());
        assertEquals(2, contact.getEmails().size());
        assertEquals("work@example.com", contact.getEmails().get("work"));
        assertEquals("personal@example.com", contact.getEmails().get("personal"));
        assertEquals(2, contact.getPhoneNumbers().size());
        assertEquals("+111222333", contact.getPhoneNumbers().get("work"));
        assertEquals("+444555666", contact.getPhoneNumbers().get("home"));
    }
}
