package com.saif.contactmanagement.util;

import com.saif.contactmanagement.entity.Contact;
import java.util.*;

public class CsvHelper {

    private static final String CSV_HEADER = "firstName,lastName,title,email,phoneNumber,company,address,notes,favorite,emails,phoneNumbers";

    private CsvHelper() {
        // Prevent instantiation
    }

    public static String contactsToCsv(List<Contact> contacts) {
        if (contacts == null || contacts.isEmpty()) {
            return CSV_HEADER + "\n";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(CSV_HEADER).append("\n");

        for (Contact contact : contacts) {
            sb.append(escapeCsvField(contact.getFirstName())).append(",");
            sb.append(escapeCsvField(contact.getLastName())).append(",");
            sb.append(escapeCsvField(contact.getTitle())).append(",");
            sb.append(escapeCsvField(contact.getEmail())).append(",");
            sb.append(escapeCsvField(contact.getPhoneNumber())).append(",");
            sb.append(escapeCsvField(contact.getCompany())).append(",");
            sb.append(escapeCsvField(contact.getAddress())).append(",");
            sb.append(escapeCsvField(contact.getNotes())).append(",");
            sb.append(contact.getFavorite() != null ? contact.getFavorite() : "false").append(",");
            sb.append(escapeCsvField(serializeMap(contact.getEmails()))).append(",");
            sb.append(escapeCsvField(serializeMap(contact.getPhoneNumbers()))).append("\n");
        }

        return sb.toString();
    }

    public static List<Contact> csvToContacts(String csvContent) {
        List<Contact> contacts = new ArrayList<>();
        if (csvContent == null || csvContent.isBlank()) {
            return contacts;
        }

        if (csvContent.startsWith("\uFEFF")) {
            csvContent = csvContent.substring(1);
        }

        try {
            org.apache.commons.csv.CSVFormat csvFormat = org.apache.commons.csv.CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build();

            org.apache.commons.csv.CSVParser parser = org.apache.commons.csv.CSVParser.parse(csvContent, csvFormat);
            List<org.apache.commons.csv.CSVRecord> records;
            try {
                records = parser.getRecords();
            } catch (Exception e) {
                throw new IllegalArgumentException("Malformed CSV: Failed to parse records", e);
            }

            for (org.apache.commons.csv.CSVRecord record : records) {
                // Skip empty lines
                if (record.size() == 0 || (record.size() == 1 && (record.get(0) == null || record.get(0).isBlank()))) {
                    continue;
                }

                Contact contact = new Contact();
                contact.setFirstName(getRecordField(record, "firstName"));
                contact.setLastName(getRecordField(record, "lastName"));
                contact.setTitle(getRecordField(record, "title"));
                contact.setEmail(getRecordField(record, "email"));
                contact.setPhoneNumber(getRecordField(record, "phoneNumber"));
                contact.setCompany(getRecordField(record, "company"));
                contact.setAddress(getRecordField(record, "address"));
                contact.setNotes(getRecordField(record, "notes"));
                contact.setFavorite(Boolean.parseBoolean(getRecordField(record, "favorite")));
                contact.setEmails(deserializeMap(getRecordField(record, "emails")));
                contact.setPhoneNumbers(deserializeMap(getRecordField(record, "phoneNumbers")));

                contacts.add(contact);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Malformed CSV: " + e.getMessage(), e);
        }

        return contacts;
    }

    private static String getRecordField(org.apache.commons.csv.CSVRecord record, String headerName) {
        if (record.isMapped(headerName) && record.isSet(headerName)) {
            String val = record.get(headerName);
            return val != null ? val.trim() : "";
        }
        return "";
    }

    private static String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    static String serializeMap(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize map to JSON", e);
        }
    }

    static Map<String, String> deserializeMap(String data) {
        if (data == null || data.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(data, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize map from JSON", e);
        }
    }
}
