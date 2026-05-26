package com.example.projectmanager.controller;

import com.example.projectmanager.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@RequestParam String username,
                                      @RequestParam String email,
                                      @RequestParam String password,
                                      RedirectAttributes redirectAttributes) {
        try {
            userService.register(username, email, password);
            redirectAttributes.addFlashAttribute("message",
                    "Регистрация успешна! Проверьте почту для подтверждения email.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
        return "redirect:/login";
    }

    @GetMapping("/confirm")
    public String confirmEmail(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        boolean confirmed = userService.confirmUser(token);
        if (confirmed) {
            redirectAttributes.addFlashAttribute("message",
                    "Email подтверждён! Теперь вы можете войти со своим логином и паролем.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Неверный или устаревший токен.");
        }
        return "redirect:/login";
    }
}