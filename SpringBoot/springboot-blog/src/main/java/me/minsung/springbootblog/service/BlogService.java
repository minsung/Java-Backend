package me.minsung.springbootblog.service;

import lombok.RequiredArgsConstructor;
import me.minsung.springbootblog.domain.Article;
import me.minsung.springbootblog.dto.AddArticleRequest;
import me.minsung.springbootblog.repository.BlogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
