package com.saif.contactmanagement.service.impl;

import com.saif.contactmanagement.entity.Contact;
import com.saif.contactmanagement.entity.User;
import com.saif.contactmanagement.security.CurrentUserProvider;
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
@SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
class ContactServiceImplTest {

    @Mock
    private ContactRepository contactRepository;

    private ContactServiceImpl contactService;
    private final CurrentUserProvider currentUserProvider = new CurrentUserProvider();

    private User currentUser;
    private User otherUser;
    private Contact contact;

    private void assertThrowsWithMessage(Class<? extends Throwable> expectedType, String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
        Throwable exception = assertThrows(expectedType, executable);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @BeforeEach
    void setUp() {
        contactService = new ContactServiceImpl(contactRepository, currentUserProvider);
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

        assertThrowsWithMessage(BadCredentialsException.class, "User not authenticated", () -> contactService.createContact(inputContact));
        verify(contactRepository, never()).save(any(Contact.class));
    }

    @Test
    void shouldThrowBadCredentialsWhenCreatingContactWithInvalidPrincipal() {
        mockInvalidPrincipalSecurityContext();
        Contact inputContact = Contact.builder().firstName("John").build();

        assertThrowsWithMessage(BadCredentialsException.class, "Invalid user details", () -> contactService.createContact(inputContact));
        verify(contactRepository, never()).save(any(Contact.class));
    }

    // --- Get All Contacts Tests ---

    @Test
    void shouldGetAllContactsSuccessfully() {
        mockSecurityContext(currentUser);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contact> contactPage = new PageImpl<>(Collections.singletonList(contact));
        when(contactRepository.findByUserId(1L, pageable)).thenReturn(contactPage);

        Page<Contact> result = contactService.getAllContacts(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(contact, result.getContent().get(0));
        verify(contactRepository).findByUserId(1L, pageable);
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

        assertThrowsWithMessage(ResourceNotFoundException.class, "Contact not found", () -> contactService.getContactById(999L));
    }

    @Test
    void shouldThrowResourceNotFoundWhenContactBelongsToOtherUser() {
        mockSecurityContext(currentUser);
        Contact otherContact = Contact.builder()
                .id(200L)
                .user(otherUser)
                .build();
        when(contactRepository.findById(200L)).thenReturn(Optional.of(otherContact));

        assertThrowsWithMessage(ResourceNotFoundException.class, "Contact not found", () -> contactService.getContactById(200L));
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

        assertThrowsWithMessage(ResourceNotFoundException.class, "Contact not found", () -> contactService.updateContact(999L, updatedInfo));
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

        assertThrowsWithMessage(ResourceNotFoundException.class, "Contact not found", () -> contactService.updateContact(200L, updatedInfo));
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

        assertThrowsWithMessage(ResourceNotFoundException.class, "Contact not found", () -> contactService.deleteContact(999L));
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

        assertThrowsWithMessage(ResourceNotFoundException.class, "Contact not found", () -> contactService.deleteContact(200L));
        verify(contactRepository, never()).delete(any(Contact.class));
    }

    // --- Search Contacts Tests ---

    @Test
    void shouldSearchContactsSuccessfully() {
        mockSecurityContext(currentUser);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contact> contactPage = new PageImpl<>(Collections.singletonList(contact));
        when(contactRepository.searchByUserIdAndKeyword(1L, "keyword", pageable))
                .thenReturn(contactPage);

        Page<Contact> result = contactService.searchContacts("keyword", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(contact, result.getContent().get(0));
    }

    @Test
    void shouldThrowResourceNotFoundWhenContactHasNoUser() {
        mockSecurityContext(currentUser);
        Contact contactNoUser = Contact.builder().id(300L).user(null).build();
        when(contactRepository.findById(300L)).thenReturn(Optional.of(contactNoUser));

        assertThrowsWithMessage(ResourceNotFoundException.class, "Contact not found", () -> contactService.getContactById(300L));
    }

    @Test
    void shouldThrowBadCredentialsWhenAuthenticationIsNotAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Contact unauthenticatedContact = Contact.builder().firstName("John").build();
        assertThrowsWithMessage(BadCredentialsException.class, "User not authenticated", () -> contactService.createContact(unauthenticatedContact));
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

    // --- Export / Import Contacts Tests ---

    @Test
    void shouldExportContactsSuccessfully() {
        mockSecurityContext(currentUser);
        when(contactRepository.findByUserId(1L)).thenReturn(java.util.List.of(contact));

        String csv = contactService.exportContactsToCsv();
        assertNotNull(csv);
        assertTrue(csv.contains("firstName,lastName"));
        assertTrue(csv.contains("John"));
        assertTrue(csv.contains("Doe"));
    }

    @Test
    void shouldImportContactsSuccessfully() {
        mockSecurityContext(currentUser);
        String csv = "firstName,lastName,title,email,phoneNumber,company,address,notes,favorite,emails,phoneNumbers\n"
                + "Alice,Smith,Ms.,alice@example.com,+1234567890,Tech,123 St,notes,false,,\n";

        when(contactRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        int count = contactService.importContactsFromCsv(csv);
        assertEquals(1, count);
        verify(contactRepository).saveAll(anyList());
    }

    @Test
    void shouldImportEmptyCsv() {
        mockSecurityContext(currentUser);
        int count = contactService.importContactsFromCsv("");
        assertEquals(0, count);
        verify(contactRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldThrowExceptionWhenImportingBlankFirstName() {
        mockSecurityContext(currentUser);
        String csv = "firstName,lastName,title,email,phoneNumber,company,address,notes,favorite,emails,phoneNumbers\n"
                + ",Smith,Ms.,alice@example.com,+1234567890,Tech,123 St,notes,false,,\n";

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> contactService.importContactsFromCsv(csv));
        verify(contactRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldThrowExceptionWhenImportingBlankLastName() {
        mockSecurityContext(currentUser);
        String csv = "firstName,lastName,title,email,phoneNumber,company,address,notes,favorite,emails,phoneNumbers\n"
                + "Alice,,Ms.,alice@example.com,+1234567890,Tech,123 St,notes,false,,\n";

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> contactService.importContactsFromCsv(csv));
        verify(contactRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldThrowExceptionWhenImportingFirstNameTooLong() {
        mockSecurityContext(currentUser);
        String longFirstName = "A".repeat(51);
        String csv = "firstName,lastName,title,email,phoneNumber,company,address,notes,favorite,emails,phoneNumbers\n"
                + longFirstName + ",Smith,Ms.,alice@example.com,+1234567890,Tech,123 St,notes,false,,\n";

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> contactService.importContactsFromCsv(csv));
        verify(contactRepository, never()).saveAll(anyList());
    }
}
