package com.example.projectmanager.service;

import com.example.projectmanager.model.User;
import com.example.projectmanager.repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final EmailService emailService;
    private final InMemoryUserDetailsManager inMemoryUserDetailsManager;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JdbcTemplate jdbcTemplate,
                       EmailService emailService,
                       InMemoryUserDetailsManager inMemoryUserDetailsManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.emailService = emailService;
        this.inMemoryUserDetailsManager = inMemoryUserDetailsManager;
    }

    @Transactional
    public User register(String username, String email, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email уже используется");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEnabled(false);
        user.setCreatedAt(LocalDateTime.now());
        String token = UUID.randomUUID().toString();
        user.setConfirmationToken(token);
        userRepository.save(user);

        jdbcTemplate.update(
                "INSERT INTO user_roles (user_id, role_id) SELECT ?, id FROM roles WHERE name = 'ROLE_USER'",
                user.getId()
        );

        emailService.sendVerificationEmail(email, username, token);

        System.out.println("==============================================");
        System.out.println("Зарегистрирован: " + username);
        System.out.println("Ссылка: http://localhost:8080/confirm?token=" + token);
        System.out.println("==============================================");

        return user;
    }

    public boolean confirmUser(String token) {
        var optionalUser = userRepository.findByConfirmationToken(token);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setEnabled(true);
            user.setConfirmationToken(null);
            userRepository.save(user);

            // Добавляем в память
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
                inMemoryUserDetailsManager.createUser(userDetails);
                System.out.println(">>> Пользователь " + user.getUsername() + " добавлен в память");
            } catch (Exception e) {
                inMemoryUserDetailsManager.updateUser(userDetails);
                System.out.println(">>> Пользователь " + user.getUsername() + " обновлён в памяти");
            }

            return true;
        }
        return false;
    }
}