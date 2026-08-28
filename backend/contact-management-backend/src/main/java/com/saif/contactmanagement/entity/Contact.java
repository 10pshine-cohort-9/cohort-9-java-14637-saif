package com.saif.contactmanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import java.util.HashMap;
import java.util.Map;




@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "contacts")


public class Contact {




    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String title;
    private String email;
    private String phoneNumber;
    private String company;
    private String address;
    private String notes;
    private Boolean favorite;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "contact_emails", joinColumns = @JoinColumn(name = "contact_id"))
    @MapKeyColumn(name = "label")
    @Column(name = "email")
    @BatchSize(size = 10)
    @Builder.Default
    private Map<String, String> emails = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "contact_phone_numbers", joinColumns = @JoinColumn(name = "contact_id"))
    @MapKeyColumn(name = "label")
    @Column(name = "phone_number")
    @BatchSize(size = 10)
    @Builder.Default
    private Map<String, String> phoneNumbers = new HashMap<>();



    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


}
