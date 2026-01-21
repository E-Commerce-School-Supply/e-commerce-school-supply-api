package com.e_commerce.backend.security;

import com.e_commerce.backend.entity.User;
import com.e_commerce.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (user.isDeactivated()) {
            throw new DisabledException("User account is deactivated");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail()) 
                .password(user.getPassword())
                .authorities(user.getRole() != null ? user.getRole() : "USER")
                .build();
    }
}