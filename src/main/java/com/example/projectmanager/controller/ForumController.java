package com.example.projectmanager.controller;

import com.example.projectmanager.model.*;
import com.example.projectmanager.repository.ForumPostRepository;
import com.example.projectmanager.repository.UserRepository;
import com.example.projectmanager.service.ForumService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/forum")
public class ForumController {
    private final ForumService forumService;
    private final UserRepository userRepository;
    private final ForumPostRepository postRepository;

    public ForumController(ForumService forumService,
                           UserRepository userRepository,
                           ForumPostRepository postRepository) {
        this.forumService = forumService;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @GetMapping("/project/{projectId}")
    public String categories(@PathVariable Long projectId, Model model, Authentication authentication) {
        addUserInfo(model, authentication);
        model.addAttribute("categories", forumService.getCategoriesByProject(projectId));
        model.addAttribute("projectId", projectId);
        return "forum-categories";
    }

    @PostMapping("/category")
    public String createCategory(@RequestParam Long projectId, @RequestParam String name) {
        ForumCategory category = new ForumCategory();
        category.setProjectId(projectId);
        category.setName(name);
        forumService.createCategory(category);
        return "redirect:/forum/project/" + projectId;
    }

    @GetMapping("/category/{categoryId}")
    public String topics(@PathVariable Long categoryId, Model model, Authentication authentication) {
        addUserInfo(model, authentication);
        model.addAttribute("topics", forumService.getTopicsByCategory(categoryId));
        model.addAttribute("categoryId", categoryId);
        return "forum-topics";
    }

    @PostMapping("/topic")
    public String createTopic(@RequestParam Long categoryId, @RequestParam String title, Authentication authentication) {
        ForumTopic topic = new ForumTopic();
        topic.setCategoryId(categoryId);
        topic.setTitle(title);

        if (authentication != null) {
            String username = authentication.getName();
            var userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                topic.setCreatorId(userOpt.get().getId());
            } else {
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setEmail(username + "@example.com");
                newUser.setPasswordHash("x");
                newUser.setEnabled(true);
                User saved = userRepository.save(newUser);
                topic.setCreatorId(saved.getId());
            }
        }
        if (topic.getCreatorId() == null) {
            topic.setCreatorId(1L);
        }

        forumService.createTopic(topic);
        return "redirect:/forum/category/" + categoryId;
    }

    @GetMapping("/topic/{topicId}")
    public String posts(@PathVariable Long topicId, Model model, Authentication authentication) {
        addUserInfo(model, authentication);

        Long currentUserId = null;
        String currentUserRole = "ROLE_USER";
        String currentUsername = null;

        if (authentication != null) {
            currentUsername = authentication.getName();
            currentUserRole = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("ROLE_USER");

            var userOpt = userRepository.findByUsername(currentUsername);
            if (userOpt.isPresent()) {
                currentUserId = userOpt.get().getId();
            } else {
                User newUser = new User();
                newUser.setUsername(currentUsername);
                newUser.setEmail(currentUsername + "@example.com");
                newUser.setPasswordHash("x");
                newUser.setEnabled(true);
                User saved = userRepository.save(newUser);
                currentUserId = saved.getId();
            }
        }

        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("currentUserRole", currentUserRole);
        model.addAttribute("isAdmin", "ROLE_ADMIN".equals(currentUserRole));
        model.addAttribute("isManager", "ROLE_MANAGER".equals(currentUserRole));

        List<ForumPost> posts = forumService.getPostsByTopic(topicId);

        Map<Long, String> userNames = new HashMap<>();
        Map<Long, Boolean> userIsAdmin = new HashMap<>();

        for (ForumPost post : posts) {
            if (!userNames.containsKey(post.getUserId())) {
                String name = getUsernameById(post.getUserId());
                userNames.put(post.getUserId(), name);
                userIsAdmin.put(post.getUserId(), "admin".equals(name));
            }
        }

        model.addAttribute("posts", posts);
        model.addAttribute("userNames", userNames);
        model.addAttribute("userIsAdmin", userIsAdmin);
        model.addAttribute("topicId", topicId);
        return "forum-posts";
    }

    @PostMapping("/post")
    public String createPost(@RequestParam Long topicId, @RequestParam String body, Authentication authentication) {
        ForumPost post = new ForumPost();
        post.setTopicId(topicId);
        post.setBody(body);

        if (authentication != null) {
            String username = authentication.getName();
            var userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                post.setUserId(userOpt.get().getId());
            } else {
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setEmail(username + "@example.com");
                newUser.setPasswordHash("x");
                newUser.setEnabled(true);
                User saved = userRepository.save(newUser);
                post.setUserId(saved.getId());
            }
        }
        if (post.getUserId() == null) {
            post.setUserId(1L);
        }

        forumService.createPost(post);

        System.out.println(">>> Сообщение от: " + authentication.getName() + ", userId: " + post.getUserId());

        return "redirect:/forum/topic/" + topicId;
    }

    @PostMapping("/post/{id}/delete")
    public String deletePost(@PathVariable Long id, @RequestParam Long topicId) {
        postRepository.deleteById(id);
        return "redirect:/forum/topic/" + topicId;
    }

    private String getUsernameById(Long userId) {
        if (userId == null) return "Гость";

        var user = userRepository.findById(userId);
        return user.map(User::getUsername).orElse("Пользователь #" + userId);
    }

    private void addUserInfo(Model model, Authentication authentication) {
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("");
            model.addAttribute("role", role);
        }
    }
}