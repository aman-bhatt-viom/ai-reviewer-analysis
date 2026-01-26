package com.example.viom16.repository;

import com.example.viom16.model.User;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> usersByEmail = new ConcurrentHashMap<>();

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(usersByEmail.get(email.toLowerCase()));
    }

    @Override
    public User save(User user) {
        usersByEmail.put(user.email().toLowerCase(), user);
        return user;
    }
}
