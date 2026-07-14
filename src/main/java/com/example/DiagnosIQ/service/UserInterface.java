package com.example.DiagnosIQ.service;

import com.example.DiagnosIQ.dto.request.RegisterRequest;
import com.example.DiagnosIQ.dto.response.RegisterResponse;
import com.example.DiagnosIQ.entity.User;

public interface UserInterface {
    RegisterRequest registerRequest(RegisterResponse response);

}
