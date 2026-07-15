package com.example.DiagnosIQ.entity;

import com.example.DiagnosIQ.enums.Roles;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userName;

    private String password;

    private String email;

    @Enumerated(EnumType.STRING)
    private Roles role;
}