package com.sstu.LearningManagementSystem.config;

import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username); // Или findByEmail, если логин = email
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        User u = user.get();

        // Добавляем роль как authority
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + u.getRole().name())
        );

        return new org.springframework.security.core.userdetails.User(
                u.getUsername(), u.getPassword(), authorities
        );
    }
}