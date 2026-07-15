package com.example.DiagnosIQ.dto.response;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RegisterResponse {
    private  long id ;
    private  String userName ;
    private  String email ;
    private String message ;

}
