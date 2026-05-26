package com.example.projectmanager.config;

import com.example.projectmanager.model.User;
import com.example.projectmanager.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Set;

@Configuration
public class UserLoader {

    @Bean
    CommandLineRunner loadUsers(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                InMemoryUserDetailsManager inMemory) {
        return args -> {
            // Загружаем всех активных пользователей из БД в память
            Iterable<User> users = userRepository.findAll();
            for (User user : users) {
                if (user.isEnabled()) {
                    Set<String> roles = userRepository.findRoleNamesByUserId(user.getId());
                    if (roles.isEmpty()) {
                        roles = Set.of("ROLE_USER");
                    }

                    UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                            .username(user.getUsername())
                            .password(user.getPasswordHash())
                            .roles(roles.stream()
                                    .map(r -> r.replace("ROLE_", ""))
                                    .toArray(String[]::new))
                            .build();

                    try {
                        inMemory.createUser(userDetails);
                        System.out.println(">>> Загружен пользователь из БД: " + user.getUsername());
                    } catch (Exception e) {
                        // Пользователь уже существует в памяти
                    }
                }
            }
        };
    }
}
