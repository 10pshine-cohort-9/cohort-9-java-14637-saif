package com.saif.contactmanagement.util;

import com.saif.contactmanagement.entity.Contact;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class CsvHelperTest {

    @Test
    void testConstructorIsPrivate() throws Exception {
        Constructor<CsvHelper> constructor = CsvHelper.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        CsvHelper instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test
    void testContactsToCsv_EmptyOrNull() {
        String csvNull = CsvHelper.contactsToCsv(null);
        assertTrue(csvNull.contains("firstName,lastName"));

        String csvEmpty = CsvHelper.contactsToCsv(List.of());
        assertTrue(csvEmpty.contains("firstName,lastName"));
    }

    @Test
    void testCsvToContacts_EmptyOrNull() {
        assertTrue(CsvHelper.csvToContacts(null).isEmpty());
        assertTrue(CsvHelper.csvToContacts("   ").isEmpty());
        assertTrue(CsvHelper.csvToContacts("firstName,lastName").isEmpty());
    }

    @Test
    void testContactsToCsv_Normal() {
        Contact contact = new Contact();
        contact.setFirstName("John");
        contact.setLastName("Doe");
        contact.setTitle("Mr.");
        contact.setEmail("john.doe@example.com");
        contact.setPhoneNumber("+1234567890");
        contact.setCompany("Tech Corp, Inc."); // contains comma, should be quoted
        contact.setAddress("123 \"Main\" St");   // contains quotes, should be escaped
        contact.setNotes("Line1\nLine2");        // contains newline, should be quoted
        contact.setFavorite(true);

        Map<String, String> emails = new HashMap<>();
        emails.put("work", "john@work.com");
        emails.put("home", "john@home.com");
        contact.setEmails(emails);

        Map<String, String> phones = new HashMap<>();
        phones.put("work", "999888");
        contact.setPhoneNumbers(phones);

        String csv = CsvHelper.contactsToCsv(List.of(contact));
        assertNotNull(csv);

        List<Contact> parsed = CsvHelper.csvToContacts(csv);
        assertEquals(1, parsed.size());
        Contact result = parsed.get(0);

        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("Mr.", result.getTitle());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("+1234567890", result.getPhoneNumber());
        assertEquals("Tech Corp, Inc.", result.getCompany());
        assertEquals("123 \"Main\" St", result.getAddress());
        assertEquals("Line1\nLine2", result.getNotes().replace("\r\n", "\n"));
        assertTrue(result.getFavorite());

        assertEquals(2, result.getEmails().size());
        assertEquals("john@work.com", result.getEmails().get("work"));
        assertEquals("john@home.com", result.getEmails().get("home"));

        assertEquals(1, result.getPhoneNumbers().size());
        assertEquals("999888", result.getPhoneNumbers().get("work"));
    }

    @Test
    void testCsvToContacts_InvalidOrFewerFields() {
        // Line with fewer fields or empty fields
        String csv = "firstName,lastName,title,email,phoneNumber,company,address,notes,favorite,emails,phoneNumbers\n"
                + "Alice,,,,,,,,,,\n" // only 1 value (Alice) and trailing commas
                + "Bob,Smith\n" // only 2 fields
                + "   \n" // blank line
                + ",,,,,,,,,,\n"; // empty fields

        List<Contact> contacts = CsvHelper.csvToContacts(csv);
        assertEquals(3, contacts.size()); // Alice, Bob, and the last line (which has empty firstName but exists)
        
        assertEquals("Alice", contacts.get(0).getFirstName());
        assertEquals("", contacts.get(0).getLastName());
        
        assertEquals("Bob", contacts.get(1).getFirstName());
        assertEquals("Smith", contacts.get(1).getLastName());

        assertEquals("", contacts.get(2).getFirstName());
        assertEquals("", contacts.get(2).getLastName());
    }

    @Test
    void testDeserializeMap_Malformed() {
        String csv = "firstName,lastName,title,email,phoneNumber,company,address,notes,favorite,emails,phoneNumbers\n"
                + "Alice,,,,,,,,,malformed_json_here,phone\n";
        assertThrows(IllegalArgumentException.class, () -> CsvHelper.csvToContacts(csv));
    }

    @Test
    void testMapSerialization_RoundTripWithSpecialCharacters() {
        Map<String, String> original = new HashMap<>();
        original.put("work|key:label", "value:with|pipes:andcolons");
        original.put("another:key|", "value|:");

        String serialized = CsvHelper.serializeMap(original);
        Map<String, String> deserialized = CsvHelper.deserializeMap(serialized);

        assertEquals(original, deserialized);
    }
}
