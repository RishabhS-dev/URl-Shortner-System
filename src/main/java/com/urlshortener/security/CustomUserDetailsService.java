package com.urlshortener.security;

import com.urlshortener.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/**
 * Tells Spring Security HOW to load a user from our database.
 * Spring calls this during authentication to get user details.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(user -> User.withUsername(user.getEmail())
                        .password(user.getPassword()) // BCrypt hash
                        .roles("USER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("No user with email: " + email));
    }
}