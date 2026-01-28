package com.example.viom16.repository;

import com.example.viom16.model.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);

    User save(User user);
}
