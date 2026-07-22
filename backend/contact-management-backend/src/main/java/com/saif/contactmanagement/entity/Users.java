package com.saif.contactmanagement.entity;

import jakarta.persistence.*;
import lombok.*;



@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Users {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

private String firstName;

private String lastName;

private String email;

private String phoneNumber;

private String password;
}
