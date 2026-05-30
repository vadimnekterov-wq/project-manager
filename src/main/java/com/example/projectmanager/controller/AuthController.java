package com.example.projectmanager.controller;

import com.example.projectmanager.model.User;
import com.example.projectmanager.repository.UserRepository;
import com.example.projectmanager.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @GetMapping("/login")
    public String loginForm(Model model, HttpSession session) {
        String error = (String) session.getAttribute("loginError");
        if (error != null) {
            model.addAttribute("loginError", error);
            session.removeAttribute("loginError");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           Model model) {
        var existingBlocked = userRepository.findByUsername(username);
        if (existingBlocked.isPresent() && existingBlocked.get().isBlocked()) {
            model.addAttribute("error", "⛔ Этот профиль заблокирован!");
            return "register";
        }

        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Пользователь с таким именем уже существует!");
            return "register";
        }

        String role;
        if ("admin".equalsIgnoreCase(username)) role = "ROLE_ADMIN";
        else if ("manager".equalsIgnoreCase(username)) role = "ROLE_MANAGER";
        else role = "ROLE_USER";

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEnabled(true);
        user.setBlocked(false);
        String token = UUID.randomUUID().toString();
        user.setConfirmationToken(token);
        userRepository.save(user);

        emailService.sendVerification(email, token);
        System.out.println("Зарегистрирован: " + username + ", токен: " + token);

        return "redirect:/login?registered";
    }

    @GetMapping("/confirm")
    public String confirm(@RequestParam String token, RedirectAttributes redirect) {
        var userOpt = userRepository.findByConfirmationToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isBlocked()) {
                redirect.addFlashAttribute("error", "⛔ Ваш аккаунт заблокирован!");
                return "redirect:/login";
            }
            user.setEnabled(true);
            user.setConfirmationToken(null);
            userRepository.save(user);
            redirect.addFlashAttribute("message", "Email подтверждён!");
        }
        return "redirect:/login";
    }
}
