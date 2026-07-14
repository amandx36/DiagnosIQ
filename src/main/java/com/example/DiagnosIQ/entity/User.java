package com.example.DiagnosIQ.entity;

import com.example.DiagnosIQ.enums.Role;
import jakarta.persistence.Table;
import lombok.Data;

@Table
@Data
public class   User {

    private String userName;
    private String password;
    private String email;
    private Role role;

}