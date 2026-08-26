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

        List<List<String>> records = parseCsv(csvContent);
        if (records.isEmpty()) {
            return contacts;
        }

        // First record is header, so start from index 1
        for (int i = 1; i < records.size(); i++) {
            List<String> fields = records.get(i);
            if (fields.isEmpty() || (fields.size() == 1 && fields.get(0).isBlank())) {
                continue;
            }

            Contact contact = new Contact();
            contact.setFirstName(getField(fields, 0));
            contact.setLastName(getField(fields, 1));
            contact.setTitle(getField(fields, 2));
            contact.setEmail(getField(fields, 3));
            contact.setPhoneNumber(getField(fields, 4));
            contact.setCompany(getField(fields, 5));
            contact.setAddress(getField(fields, 6));
            contact.setNotes(getField(fields, 7));
            contact.setFavorite(Boolean.parseBoolean(getField(fields, 8)));
            contact.setEmails(deserializeMap(getField(fields, 9)));
            contact.setPhoneNumbers(deserializeMap(getField(fields, 10)));

            contacts.add(contact);
        }

        return contacts;
    }

    private static String getField(List<String> fields, int index) {
        if (index < fields.size()) {
            String val = fields.get(index);
            return val != null ? val.trim() : null;
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

    private static String serializeMap(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append(entry.getKey()).append(":").append(entry.getValue());
        }
        return sb.toString();
    }

    private static Map<String, String> deserializeMap(String data) {
        Map<String, String> map = new HashMap<>();
        if (data == null || data.isBlank()) {
            return map;
        }
        String[] entries = data.split("\\|");
        for (String entry : entries) {
            if (entry.isBlank()) {
                continue;
            }
            int colonIndex = entry.indexOf(':');
            if (colonIndex > 0) {
                String key = entry.substring(0, colonIndex).trim();
                String value = entry.substring(colonIndex + 1).trim();
                map.put(key, value);
            }
        }
        return map;
    }

    private static List<List<String>> parseCsv(String csvContent) {
        List<List<String>> records = new ArrayList<>();
        List<String> curRecord = new ArrayList<>();
        StringBuilder curVal = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < csvContent.length(); i++) {
            char ch = csvContent.charAt(i);
            if (inQuotes) {
                if (ch == '\"') {
                    if (i + 1 < csvContent.length() && csvContent.charAt(i + 1) == '\"') {
                        curVal.append('\"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    curVal.append(ch);
                }
            } else {
                if (ch == '\"') {
                    inQuotes = true;
                } else if (ch == ',') {
                    curRecord.add(curVal.toString());
                    curVal.setLength(0);
                } else if (ch == '\n' || ch == '\r') {
                    curRecord.add(curVal.toString());
                    curVal.setLength(0);
                    records.add(new ArrayList<>(curRecord));
                    curRecord.clear();
                    // Handle \r\n
                    if (ch == '\r' && i + 1 < csvContent.length() && csvContent.charAt(i + 1) == '\n') {
                        i++;
                    }
                } else {
                    curVal.append(ch);
                }
            }
        }

        // Add the last field and record if any
        if (curVal.length() > 0 || !curRecord.isEmpty()) {
            curRecord.add(curVal.toString());
            records.add(curRecord);
        }
        return records;
    }
}
