package com.example.projectmanager.service;

import com.example.projectmanager.model.*;
import com.example.projectmanager.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ForumService {
    private final ForumCategoryRepository categoryRepository;
    private final ForumTopicRepository topicRepository;
    private final ForumPostRepository postRepository;

    public ForumService(ForumCategoryRepository categoryRepository,
                        ForumTopicRepository topicRepository,
                        ForumPostRepository postRepository) {
        this.categoryRepository = categoryRepository;
        this.topicRepository = topicRepository;
        this.postRepository = postRepository;
    }

    public ForumCategory createCategory(ForumCategory category) {
        return categoryRepository.save(category);
    }

    public List<ForumCategory> getCategoriesByProject(Long projectId) {
        return categoryRepository.findByProjectId(projectId);
    }

    public ForumTopic createTopic(ForumTopic topic) {
        topic.setCreatedAt(LocalDateTime.now());
        return topicRepository.save(topic);
    }

    public List<ForumTopic> getTopicsByCategory(Long categoryId) {
        return topicRepository.findByCategoryId(categoryId);
    }

    public ForumPost createPost(ForumPost post) {
        post.setCreatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    public List<ForumPost> getPostsByTopic(Long topicId) {
        return postRepository.findByTopicId(topicId);
    }
}