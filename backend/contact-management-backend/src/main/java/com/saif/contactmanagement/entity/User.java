package com.saif.contactmanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;



@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, columnDefinition = "int default 1")
    @Builder.Default
    private int credentialVersion = 1;

    @OneToMany(mappedBy = "user")
    private List<Contact> contacts;

}
