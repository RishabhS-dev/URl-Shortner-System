package com.urlshortener.repository;

import com.urlshortener.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data generates: SELECT * FROM users WHERE email = ? — uses the unique index
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}