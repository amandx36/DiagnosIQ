package  com.example.DiagnosIQ.service.impl;

import com.example.DiagnosIQ.dto.request.RegisterRequest;
import com.example.DiagnosIQ.dto.response.RegisterResponse;
import com.example.DiagnosIQ.entity.User;
import com.example.DiagnosIQ.enums.Roles;
import com.example.DiagnosIQ.repository.UserRepository;
import com.example.DiagnosIQ.service.UserInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
 public class UserInterfaceImpl implements UserInterface {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public RegisterResponse registerRequest(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        if (userRepository.existsByUserName(request.getUserName())) {
            throw new IllegalArgumentException("Username already exists");
        }

        User newUser = new User();

        newUser.setUserName(request.getUserName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(Roles.PATIENCE);

        User savedUser = userRepository.save(newUser);

        RegisterResponse response = new RegisterResponse();
        response.setId(savedUser.getId());
        response.setUserName(savedUser.getUserName());
        response.setEmail(savedUser.getEmail());
        response.setMessage("User Created Successfully");

        return response;
    }
}