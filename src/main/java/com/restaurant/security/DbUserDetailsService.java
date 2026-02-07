package com.restaurant.security;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.restaurant.entity.UserEntity;
import com.restaurant.repository.UserRepository;

@Service
public class DbUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public DbUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity u = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (u.getIsActive() != null && !u.getIsActive()) {
            throw new DisabledException("User disabled: " + username);
        }

        return User.builder()
                .username(u.getUsername())
                .password(u.getPasswordHash())   
                .roles(u.getRole().name())       
                .build();
    }
}
