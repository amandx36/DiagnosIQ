package com.example.DiagnosIQ.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message="Username must be present")
    @Size(max = 20,min = 5)
    private String userName ;

    @Email
    @NotBlank(message = "Email  is required ")
    private  String email ;

    @NotBlank(message = "Password is required")
    @Size(min = 6 , message = "Password must be greater than 6 digits ")
    private  String password;
}
