package com.example.viom16.controller;

import com.example.viom16.model.User;
import com.example.viom16.model.UserRegistrationRequest;
import com.example.viom16.model.UserRegistrationResponse;
import com.example.viom16.service.UserRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserRegistrationController {
    private final UserRegistrationService registrationService;

    public UserRegistrationController(UserRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        User user = registrationService.register(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UserRegistrationResponse(user.id()));
    }
}
