package com.saif.contactmanagement.entity;

import jakarta.persistence.*;
import lombok.*;




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



    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


}