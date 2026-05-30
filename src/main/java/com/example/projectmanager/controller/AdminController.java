package com.example.projectmanager.controller;

import com.example.projectmanager.model.User;
import com.example.projectmanager.repository.UserRepository;
import com.example.projectmanager.service.EmailService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final EmailService emailService;

    public AdminController(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @GetMapping("/users")
    public String listUsers(Model model, Authentication auth) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("username", auth.getName());
        model.addAttribute("currentUser", auth.getName());
        return "admin-users";
    }

    @GetMapping("/users/search")
    public String searchUsers(@RequestParam String query, Model model, Authentication auth) {
        model.addAttribute("users", userRepository.findByUsernameContaining(query));
        model.addAttribute("username", auth.getName());
        model.addAttribute("query", query);
        return "admin-users";
    }

    @PostMapping("/block/{id}")
    public String blockUser(@PathVariable Long id,
                            @RequestParam String reason,
                            RedirectAttributes redirect) {
        User user = userRepository.findById(id).orElseThrow();
        user.setBlocked(true);
        user.setBlockedReason(reason);
        user.setEnabled(false);
        userRepository.save(user);
        emailService.sendBlockedNotification(user.getEmail(), user.getUsername(), reason);
        redirect.addFlashAttribute("message", "Пользователь " + user.getUsername() + " заблокирован");
        return "redirect:/admin/users";
    }

    @PostMapping("/unblock/{id}")
    public String unblockUser(@PathVariable Long id, RedirectAttributes redirect) {
        User user = userRepository.findById(id).orElseThrow();
        user.setBlocked(false);
        user.setBlockedReason(null);
        user.setEnabled(true);
        userRepository.save(user);
        emailService.sendUnblockedNotification(user.getEmail(), user.getUsername());
        redirect.addFlashAttribute("message", "Пользователь " + user.getUsername() + " разблокирован");
        return "redirect:/admin/users";
    }

    @PostMapping("/promote/manager/{id}")
    public String promoteToManager(@PathVariable Long id,
                                   @RequestParam String reason,
                                   RedirectAttributes redirect) {
        User user = userRepository.findById(id).orElseThrow();
        user.setRole("ROLE_MANAGER");
        userRepository.save(user);
        emailService.sendPromotedNotification(user.getEmail(), user.getUsername(), "ROLE_MANAGER", reason);
        redirect.addFlashAttribute("message", user.getUsername() + " повышен до Менеджера");
        return "redirect:/admin/users";
    }

    @PostMapping("/promote/admin/{id}")
    public String promoteToAdmin(@PathVariable Long id,
                                 @RequestParam String reason,
                                 RedirectAttributes redirect) {
        User user = userRepository.findById(id).orElseThrow();
        user.setRole("ROLE_ADMIN");
        userRepository.save(user);
        emailService.sendPromotedNotification(user.getEmail(), user.getUsername(), "ROLE_ADMIN", reason);
        redirect.addFlashAttribute("message", user.getUsername() + " повышен до Админа");
        return "redirect:/admin/users";
    }

    @PostMapping("/demote/user/{id}")
    public String demoteToUser(@PathVariable Long id,
                               @RequestParam String reason,
                               RedirectAttributes redirect) {
        User user = userRepository.findById(id).orElseThrow();
        if (user.isMainAdmin()) {
            redirect.addFlashAttribute("error", "Нельзя понизить главного админа!");
            return "redirect:/admin/users";
        }
        user.setRole("ROLE_USER");
        userRepository.save(user);
        emailService.sendDemotedNotification(user.getEmail(), user.getUsername(), "ROLE_USER", reason);
        redirect.addFlashAttribute("message", user.getUsername() + " понижен до Пользователя");
        return "redirect:/admin/users";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id,
                             @RequestParam String reason,
                             RedirectAttributes redirect) {
        User user = userRepository.findById(id).orElseThrow();
        if (user.isMainAdmin()) {
            redirect.addFlashAttribute("error", "Нельзя удалить главного админа!");
            return "redirect:/admin/users";
        }
        String username = user.getUsername();
        String email = user.getEmail();
        userRepository.deleteById(id);
        emailService.sendDeletedNotification(email, username, reason);
        redirect.addFlashAttribute("message", "Пользователь " + username + " удалён");
        return "redirect:/admin/users";
    }
}
