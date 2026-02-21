package com.pocketnews.service;

import com.pocketnews.dto.NewsDTO;
import com.pocketnews.dto.NewsCreateRequest;
import com.pocketnews.entity.News;
import com.pocketnews.entity.Category;
import com.pocketnews.exception.ResourceNotFoundException;
import com.pocketnews.repository.NewsRepository;
import com.pocketnews.repository.CategoryRepository;
import com.pocketnews.repository.LikeRepository;
import com.pocketnews.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsService {

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    public Page<NewsDTO> getAllNews(Pageable pageable) {
        Page<News> news = newsRepository.findAll(pageable);
        List<NewsDTO> dtos = news.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, news.getTotalElements());
    }

    public Page<NewsDTO> getNewsByCategory(Long categoryId, Pageable pageable) {
        Page<News> news = newsRepository.findByCategoryId(categoryId, pageable);
        List<NewsDTO> dtos = news.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, news.getTotalElements());
    }

    public Page<NewsDTO> getFeaturedNews(Pageable pageable) {
        Page<News> news = newsRepository.findByIsFeatured(true, pageable);
        List<NewsDTO> dtos = news.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, news.getTotalElements());
    }

    public NewsDTO getNewsById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + id));
        return mapToDTO(news);
    }

    public NewsDTO createNews(NewsCreateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        News news = new News();
        news.setCategory(category);
        news.setTitle(request.getTitle());
        news.setDescription(request.getDescription());
        news.setContent(request.getContent());
        news.setImageUrl(request.getImageUrl());
        news.setSourceUrl(request.getSourceUrl());
        news.setSourceName(request.getSourceName());
        news.setAuthor(request.getAuthor());
        news.setIsFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false);
        news.setPublishedAt(LocalDateTime.now());

        news = newsRepository.save(news);
        return mapToDTO(news);
    }

    public NewsDTO updateNews(Long id, NewsCreateRequest request) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        news.setCategory(category);
        news.setTitle(request.getTitle());
        news.setDescription(request.getDescription());
        news.setContent(request.getContent());
        news.setImageUrl(request.getImageUrl());
        news.setSourceUrl(request.getSourceUrl());
        news.setSourceName(request.getSourceName());
        news.setAuthor(request.getAuthor());
        news.setIsFeatured(request.getIsFeatured());

        news = newsRepository.save(news);
        return mapToDTO(news);
    }

    public void deleteNews(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + id));
        newsRepository.delete(news);
    }

    private NewsDTO mapToDTO(News news) {
        return new NewsDTO(
                news.getId(),
                news.getCategory().getId(),
                news.getCategory().getName(),
                news.getTitle(),
                news.getDescription(),
                news.getContent(),
                news.getImageUrl(),
                news.getSourceUrl(),
                news.getSourceName(),
                news.getAuthor(),
                news.getIsFeatured(),
                news.getViewsCount(),
                likeRepository.countByNewsId(news.getId()),
                commentRepository.countByNewsId(news.getId()),
                news.getPublishedAt(),
                news.getCreatedAt(),
                news.getUpdatedAt()
        );
    }
}

