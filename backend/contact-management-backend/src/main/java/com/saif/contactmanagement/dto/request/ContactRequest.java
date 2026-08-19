package com.saif.contactmanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Size(max = 50, message = "Title must not exceed 50 characters")
    private String title;

    @Email(message = "Please enter a valid email address")
    private String email;

    @Pattern(
            regexp = "^$|^\\+?\\d{10,15}$",
            message = "Phone number must contain 10 to 15 digits and may start with +"
    )
    private String phoneNumber;

    @Size(max = 100, message = "Company name must not exceed 100 characters")
    private String company;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    private Boolean favorite;
}
