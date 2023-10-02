package me.minsung.springbootblog.service;

import lombok.RequiredArgsConstructor;
import me.minsung.springbootblog.domain.Article;
import me.minsung.springbootblog.dto.AddArticleRequest;
import me.minsung.springbootblog.repository.BlogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BlogService {
    private final BlogRepository blogRepository;

    public Article save(AddArticleRequest request) {
        return blogRepository.save(request.toEntity());
    }

    public List<Article> findAll() {
        return blogRepository.findAll();
    }

    public Article findById(long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));
    }
}
