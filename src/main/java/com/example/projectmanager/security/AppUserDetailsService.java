package com.example.projectmanager.security;

import com.example.projectmanager.model.User;
import com.example.projectmanager.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));

        if (user.isBlocked()) {
            throw new RuntimeException("⛔ Ваш аккаунт заблокирован! Причина: " +
                    (user.getBlockedReason() != null ? user.getBlockedReason() : "нарушение правил"));
        }

        String role = user.getRole();
        if (role == null || role.isEmpty()) role = "ROLE_USER";

        System.out.println(">>> Вход: " + username + ", роль: " + role);

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true, true, true,
                List.of(new SimpleGrantedAuthority(role))
        );
    }
}