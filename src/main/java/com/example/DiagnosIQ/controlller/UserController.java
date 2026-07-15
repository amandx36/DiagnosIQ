package com.example.DiagnosIQ.controlller;


import com.example.DiagnosIQ.dto.request.RegisterRequest;
import com.example.DiagnosIQ.dto.response.RegisterResponse;
import com.example.DiagnosIQ.service.UserInterface;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;



@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

   private final UserInterface  userService;
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(
            @Valid @RequestBody RegisterRequest request) {

        RegisterResponse response = userService.registerRequest(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }



}
