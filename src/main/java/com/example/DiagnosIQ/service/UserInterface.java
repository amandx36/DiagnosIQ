package com.example.DiagnosIQ.service;

import com.example.DiagnosIQ.dto.request.RegisterRequest;
import com.example.DiagnosIQ.dto.response.RegisterResponse;

public interface UserInterface {

    RegisterResponse registerRequest(RegisterRequest request);

}