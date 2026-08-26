package com.saif.contactmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saif.contactmanagement.dto.request.ContactRequest;
import com.saif.contactmanagement.entity.Contact;
import com.saif.contactmanagement.entity.User;
import com.saif.contactmanagement.exception.GlobalExceptionHandler;
import com.saif.contactmanagement.exception.ResourceNotFoundException;
import com.saif.contactmanagement.service.ContactService;
import com.saif.contactmanagement.service.impl.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
class ContactControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ContactService contactService;
    @Mock
    private jakarta.servlet.http.HttpServletRequest httpServletRequest;

    private ContactController contactController;
    private final com.saif.contactmanagement.security.CurrentUserProvider currentUserProvider = new com.saif.contactmanagement.security.CurrentUserProvider();

    private User currentUser;
    private Contact contact;
    private ContactRequest validRequest;

    @BeforeEach
    void setUp() {
        contactController = new ContactController(contactService, currentUserProvider, httpServletRequest);
        mockMvc = MockMvcBuilders.standaloneSetup(contactController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        currentUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .build();

        contact = Contact.builder()
                .id(100L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phoneNumber("+1234567890")
                .user(currentUser)
                .favorite(false)
                .build();

        validRequest = new ContactRequest(
                "John",
                "Doe",
                "Manager",
                "john@example.com",
                "+1234567890",
                "Company Inc",
                "123 Main St",
                "Some notes",
                false
        );
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

    // --- Create Contact Tests ---

    @Test
    void shouldCreateContactSuccessfully() throws Exception {
        when(contactService.createContact(any(Contact.class))).thenReturn(contact);

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.userId").value(1L));

        verify(contactService).createContact(any(Contact.class));
    }

    @Test
    void shouldReturnBadRequestWhenCreateContactValidationFails() throws Exception {
        ContactRequest invalidRequest = new ContactRequest(
                "", // Blank first name
                "", // Blank last name
                "Title",
                "invalid-email",
                "invalid-phone",
                "Company",
                "Address",
                "Notes",
                false
        );

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.lastName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.phoneNumber").exists());

        verify(contactService, never()).createContact(any(Contact.class));
    }

    // --- Get All Contacts Tests ---

    @Test
    void shouldGetAllContactsSuccessfully() throws Exception {
        mockSecurityContext(currentUser);
        Page<Contact> contactPage = new PageImpl<>(Collections.singletonList(contact), PageRequest.of(0, 10), 1);
        when(contactService.getAllContacts(any(Pageable.class))).thenReturn(contactPage);

        mockMvc.perform(get("/api/contacts")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "firstName")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(100L))
                .andExpect(jsonPath("$.content[0].firstName").value("John"));

        verify(contactService).getAllContacts(any(Pageable.class));
    }

    // --- Get Contact By ID Tests ---

    @Test
    void shouldGetContactByIdSuccessfully() throws Exception {
        when(contactService.getContactById(100L)).thenReturn(contact);

        mockMvc.perform(get("/api/contacts/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(contactService).getContactById(100L);
    }

    @Test
    void shouldReturnNotFoundWhenGetContactByIdThrowsResourceNotFound() throws Exception {
        when(contactService.getContactById(999L)).thenThrow(new ResourceNotFoundException("Contact not found"));

        mockMvc.perform(get("/api/contacts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Contact not found"));

        verify(contactService).getContactById(999L);
    }

    // --- Update Contact Tests ---

    @Test
    void shouldUpdateContactSuccessfully() throws Exception {
        when(contactService.updateContact(eq(100L), any(Contact.class))).thenReturn(contact);

        mockMvc.perform(put("/api/contacts/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(contactService).updateContact(eq(100L), any(Contact.class));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentContact() throws Exception {
        when(contactService.updateContact(eq(999L), any(Contact.class))).thenThrow(new ResourceNotFoundException("Contact not found"));

        mockMvc.perform(put("/api/contacts/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(contactService).updateContact(eq(999L), any(Contact.class));
    }

    @Test
    void shouldReturnBadRequestWhenUpdateContactValidationFails() throws Exception {
        ContactRequest invalidRequest = new ContactRequest(
                "", "", "Title", "invalid-email", "invalid-phone", "Company", "Address", "Notes", false
        );

        mockMvc.perform(put("/api/contacts/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(contactService, never()).updateContact(anyLong(), any(Contact.class));
    }

    // --- Delete Contact Tests ---

    @Test
    void shouldDeleteContactSuccessfully() throws Exception {
        doNothing().when(contactService).deleteContact(100L);

        mockMvc.perform(delete("/api/contacts/100"))
                .andExpect(status().isNoContent());

        verify(contactService).deleteContact(100L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentContact() throws Exception {
        doThrow(new ResourceNotFoundException("Contact not found")).when(contactService).deleteContact(999L);

        mockMvc.perform(delete("/api/contacts/999"))
                .andExpect(status().isNotFound());

        verify(contactService).deleteContact(999L);
    }

    // --- Search Contacts Tests ---

    @Test
    void shouldSearchContactsSuccessfully() throws Exception {
        mockSecurityContext(currentUser);
        Page<Contact> contactPage = new PageImpl<>(Collections.singletonList(contact), PageRequest.of(0, 10), 1);
        when(contactService.searchContacts(eq("John"), any(Pageable.class))).thenReturn(contactPage);

        mockMvc.perform(get("/api/contacts/search")
                        .param("keyword", "John")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "firstName")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(100L))
                .andExpect(jsonPath("$.content[0].firstName").value("John"));

        verify(contactService).searchContacts(eq("John"), any(Pageable.class));
    }

    @Test
    void shouldThrowBadCredentialsWhenAuthenticationIsNull() throws Exception {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/api/contacts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldThrowBadCredentialsWhenPrincipalIsInvalid() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("invalid");
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/api/contacts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldCreateContactWithDefaultFavoriteWhenNull() throws Exception {
        ContactRequest reqWithNullFavorite = new ContactRequest(
                "John", "Doe", "Manager", "john@example.com", "+1234567890",
                "Company", "Address", "Notes", null
        );

        org.mockito.ArgumentCaptor<Contact> contactCaptor = org.mockito.ArgumentCaptor.forClass(Contact.class);
        when(contactService.createContact(contactCaptor.capture())).thenReturn(contact);

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqWithNullFavorite)))
                .andExpect(status().isCreated());

        Contact capturedContact = contactCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertNotNull(capturedContact);
        org.junit.jupiter.api.Assertions.assertFalse(capturedContact.getFavorite());
    }

    @Test
    void shouldReturnResponseWhenContactHasNoUser() throws Exception {
        Contact contactNoUser = Contact.builder()
                .id(100L)
                .firstName("John")
                .lastName("Doe")
                .user(null)
                .build();

        when(contactService.getContactById(100L)).thenReturn(contactNoUser);

        mockMvc.perform(get("/api/contacts/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.userId").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void shouldGetAllContactsSortedDescending() throws Exception {
        mockSecurityContext(currentUser);
        Page<Contact> contactPage = new PageImpl<>(Collections.singletonList(contact), PageRequest.of(0, 10), 1);
        when(contactService.getAllContacts(any(Pageable.class))).thenReturn(contactPage);

        mockMvc.perform(get("/api/contacts")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "firstName")
                        .param("direction", "desc"))
                .andExpect(status().isOk());

        verify(contactService).getAllContacts(any(Pageable.class));
    }

    @Test
    void shouldSearchContactsSortedDescending() throws Exception {
        mockSecurityContext(currentUser);
        Page<Contact> contactPage = new PageImpl<>(Collections.singletonList(contact), PageRequest.of(0, 10), 1);
        when(contactService.searchContacts(eq("John"), any(Pageable.class))).thenReturn(contactPage);

        mockMvc.perform(get("/api/contacts/search")
                        .param("keyword", "John")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "firstName")
                        .param("direction", "desc"))
                .andExpect(status().isOk());

        verify(contactService).searchContacts(eq("John"), any(Pageable.class));
    }

    @Test
    void shouldCapPageSizeToTenWhenGetAllContactsRequestedWithLargerSize() throws Exception {
        mockSecurityContext(currentUser);
        Page<Contact> contactPage = new PageImpl<>(Collections.singletonList(contact), PageRequest.of(0, 10), 1);
        when(contactService.getAllContacts(any(Pageable.class))).thenReturn(contactPage);

        mockMvc.perform(get("/api/contacts")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());

        org.mockito.ArgumentCaptor<Pageable> pageableCaptor = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(contactService).getAllContacts(pageableCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(10, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void shouldCapPageSizeToTenWhenSearchContactsRequestedWithLargerSize() throws Exception {
        mockSecurityContext(currentUser);
        Page<Contact> contactPage = new PageImpl<>(Collections.singletonList(contact), PageRequest.of(0, 10), 1);
        when(contactService.searchContacts(eq("John"), any(Pageable.class))).thenReturn(contactPage);

        mockMvc.perform(get("/api/contacts/search")
                        .param("keyword", "John")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());

        org.mockito.ArgumentCaptor<Pageable> pageableCaptor = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(contactService).searchContacts(eq("John"), pageableCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(10, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void shouldCreateContactWithEmailsAndPhoneNumbersMapsSuccessfully() throws Exception {
        Map<String, String> emails = new HashMap<>();
        emails.put("work", "work@example.com");
        emails.put("personal", "personal@example.com");

        Map<String, String> phoneNumbers = new HashMap<>();
        phoneNumbers.put("work", "+1112223330");
        phoneNumbers.put("home", "+4445556660");

        ContactRequest request = new ContactRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmails(emails);
        request.setPhoneNumbers(phoneNumbers);

        Contact savedContact = Contact.builder()
                .id(100L)
                .firstName("John")
                .lastName("Doe")
                .emails(emails)
                .phoneNumbers(phoneNumbers)
                .user(currentUser)
                .favorite(false)
                .build();

        when(contactService.createContact(any(Contact.class))).thenReturn(savedContact);

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.emails.work").value("work@example.com"))
                .andExpect(jsonPath("$.emails.personal").value("personal@example.com"))
                .andExpect(jsonPath("$.phoneNumbers.work").value("+1112223330"))
                .andExpect(jsonPath("$.phoneNumbers.home").value("+4445556660"));
    }

    @Test
    void shouldReturnBadRequestWhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/api/contacts")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/contacts/search")
                        .param("keyword", "test")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSizeIsNonPositive() throws Exception {
        mockMvc.perform(get("/api/contacts")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/contacts/search")
                        .param("keyword", "test")
                        .param("size", "-5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSortByIsInvalid() throws Exception {
        mockMvc.perform(get("/api/contacts")
                        .param("sortBy", "invalidField"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/contacts/search")
                        .param("keyword", "test")
                        .param("sortBy", "password"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenDirectionIsInvalid() throws Exception {
        mockMvc.perform(get("/api/contacts")
                        .param("direction", "invalid"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/contacts/search")
                        .param("keyword", "test")
                        .param("direction", "asc123"))
                .andExpect(status().isBadRequest());
    }

    // --- Export / Import Contacts Controller Tests ---

    @Test
    void shouldExportContactsSuccessfully() throws Exception {
        when(contactService.exportContactsToCsv()).thenReturn("firstName,lastName\nJohn,Doe");

        mockMvc.perform(get("/api/contacts/export"))
                .andExpect(status().isOk())
                .andExpect(header().string(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contacts.csv\""))
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string("firstName,lastName\nJohn,Doe"));

        verify(contactService).exportContactsToCsv();
    }

    @Test
    void shouldImportContactsSuccessfully() throws Exception {
        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile(
                "file", "contacts.csv", "text/csv", "firstName,lastName\nAlice,Smith".getBytes());

        when(contactService.importContactsFromCsv(anyString())).thenReturn(1);

        mockMvc.perform(multipart("/api/contacts/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedCount").value(1));

        verify(contactService).importContactsFromCsv(anyString());
    }

    @Test
    void shouldReturnBadRequestWhenImportFileIsEmpty() throws Exception {
        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile(
                "file", "contacts.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/api/contacts/import").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnInternalServerErrorWhenReadingFileFails() throws Exception {
        org.springframework.mock.web.MockMultipartFile errorFile = new org.springframework.mock.web.MockMultipartFile(
                "file", "contacts.csv", "text/csv", "some content".getBytes()) {
            @Override
            public byte[] getBytes() throws java.io.IOException {
                throw new java.io.IOException("Simulated read failure");
            }
        };

        mockMvc.perform(multipart("/api/contacts/import").file(errorFile))
                .andExpect(status().isInternalServerError());
    }
}
