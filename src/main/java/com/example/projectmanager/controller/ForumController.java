package com.example.projectmanager.controller;

import com.example.projectmanager.model.*;
import com.example.projectmanager.repository.*;
import com.example.projectmanager.service.EmailService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/forum")
public class ForumController {
    private final ForumCategoryRepository categoryRepository;
    private final ForumTopicRepository topicRepository;
    private final ForumPostRepository postRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public ForumController(ForumCategoryRepository categoryRepository,
                           ForumTopicRepository topicRepository,
                           ForumPostRepository postRepository,
                           UserRepository userRepository,
                           EmailService emailService) {
        this.categoryRepository = categoryRepository;
        this.topicRepository = topicRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @GetMapping("/project/{projectId}")
    public String categories(@PathVariable Long projectId, Model model, Authentication auth) {
        addUserInfo(model, auth);
        model.addAttribute("categories", categoryRepository.findByProjectId(projectId));
        model.addAttribute("projectId", projectId);
        return "forum-categories";
    }

    @PostMapping("/category")
    public String createCategory(@RequestParam Long projectId, @RequestParam String name) {
        ForumCategory cat = new ForumCategory();
        cat.setProjectId(projectId);
        cat.setName(name);
        categoryRepository.save(cat);
        return "redirect:/forum/project/" + projectId;
    }

    @GetMapping("/category/{categoryId}")
    public String topics(@PathVariable Long categoryId, Model model, Authentication auth) {
        addUserInfo(model, auth);
        model.addAttribute("topics", topicRepository.findByCategoryId(categoryId));
        model.addAttribute("categoryId", categoryId);
        return "forum-topics";
    }

    @PostMapping("/topic")
    public String createTopic(@RequestParam Long categoryId, @RequestParam String title, Authentication auth) {
        ForumTopic topic = new ForumTopic();
        topic.setCategoryId(categoryId);
        topic.setTitle(title);
        topic.setCreatorId(userRepository.findByUsername(auth.getName()).get().getId());
        topicRepository.save(topic);
        return "redirect:/forum/category/" + categoryId;
    }

    @GetMapping("/topic/{topicId}")
    public String posts(@PathVariable Long topicId, Model model, Authentication auth) {
        addUserInfo(model, auth);

        List<ForumPost> posts = postRepository.findByTopicId(topicId);

        Map<Long, String> userNames = new HashMap<>();
        Map<Long, String> userRoles = new HashMap<>();
        for (ForumPost post : posts) {
            userRepository.findById(post.getUserId()).ifPresent(u -> {
                userNames.put(post.getUserId(), u.getUsername());
                String role = u.getRole();
                if (role == null || role.isEmpty()) {
                    role = "ROLE_USER";
                }
                userRoles.put(post.getUserId(), role);
            });
        }

        model.addAttribute("posts", posts);
        model.addAttribute("userNames", userNames);
        model.addAttribute("userRoles", userRoles);
        model.addAttribute("topicId", topicId);

        userRepository.findByUsername(auth.getName()).ifPresent(u -> {
            model.addAttribute("currentUserId", u.getId());
        });

        return "forum-posts";
    }

    @PostMapping("/post")
    public String createPost(@RequestParam Long topicId, @RequestParam String body, Authentication auth) {
        ForumPost post = new ForumPost();
        post.setTopicId(topicId);
        post.setBody(body);
        post.setUserId(userRepository.findByUsername(auth.getName()).get().getId());
        post.setCreatedAt(java.time.LocalDateTime.now());
        postRepository.save(post);

        // Отправляем уведомление создателю темы
        ForumTopic topic = topicRepository.findById(topicId);
        if (topic != null) {
            userRepository.findById(topic.getCreatorId()).ifPresent(creator -> {
                if (!creator.getUsername().equals(auth.getName())) {
                    emailService.sendNewForumPost(creator.getEmail(), topic.getTitle(), auth.getName(), body);
                }
            });
        }

        return "redirect:/forum/topic/" + topicId;
    }

    @PostMapping("/post/{id}/delete")
    public String deletePost(@PathVariable Long id, @RequestParam Long topicId) {
        postRepository.deleteById(id);
        return "redirect:/forum/topic/" + topicId;
    }

    private void addUserInfo(Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("isAdmin", auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        model.addAttribute("isManager", auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")));
    }
}
