package com.example.DiagnosIQ.service.impl;

import com.example.DiagnosIQ.dto.request.RegisterRequest;
import com.example.DiagnosIQ.entity.User;
import com.example.DiagnosIQ.repository.UserRepository;
import com.example.DiagnosIQ.service.UserInterface;

public class UserInterfaceImpl implements UserInterface {
    // what logic u use to save the user
public static void registerRequest(RegisterRequest request){
    User user = new User();
    final UserRepository userRepository;
    try{

    if (userRepository.existsByEmail(request.getEmail())){
        throw new Exception("Email already exist");
    }
    if (userRepository.existsByUserName(request.getUserName())){
        throw new Exception("User name already exist");
    }

    } catch (Exception e) {

        throw new RuntimeException(e);

    }

}
