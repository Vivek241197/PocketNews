package com.pocketnews.service;
import com.pocketnews.dto.NewsDTO;
import com.pocketnews.dto.NewsCreateRequest;
import com.pocketnews.entity.News;
import com.pocketnews.entity.Category;
import com.pocketnews.exception.ResourceNotFoundException;
import com.pocketnews.repository.NewsRepository;
import com.pocketnews.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class NewsService {

    private final NewsRepository newsRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryPreferenceService preferenceService;
    private final AiSummarizationService aiSummarizationService;

    public NewsService(
            NewsRepository newsRepository,
            CategoryRepository categoryRepository,
            CategoryPreferenceService preferenceService,
            AiSummarizationService aiSummarizationService) {

        this.newsRepository = newsRepository;
        this.categoryRepository = categoryRepository;
        this.preferenceService = preferenceService;
        this.aiSummarizationService = aiSummarizationService;
    }

    /* ============================================================
       MAIN FEED
       `after` — when non-null, only returns articles published after
                 that timestamp (pull-to-refresh)
       ============================================================ */

    public Page<NewsDTO> getFeed(String deviceId, Pageable pageable, LocalDateTime after) {

        List<Long> preferredCategoryIds = preferenceService.getPreferences(deviceId);

        Page<News> newsPage;

        if (preferredCategoryIds == null || preferredCategoryIds.isEmpty()) {
            newsPage = getBalancedFeed(pageable, after);
        } else {
            if (after != null) {
                newsPage = newsRepository
                        .findByCategoryIdInAndActiveTrueAndPublishedAtAfterOrderByPublishedAtDesc(
                                preferredCategoryIds, after, pageable);
            } else {
                newsPage = newsRepository
                        .findByCategoryIdInAndActiveTrueOrderByPublishedAtDesc(
                                preferredCategoryIds, pageable);
            }
        }

        return newsPage.map(this::mapToDTO);
    }

    /* ============================================================
       BALANCED FEED (equal distribution across all categories)
       ============================================================ */

    private Page<News> getBalancedFeed(Pageable pageable, LocalDateTime after) {

        int size = pageable.getPageSize();
        int page = pageable.getPageNumber();

        List<Long> categoryIds = categoryRepository.findAll()
                .stream()
                .map(Category::getId)
                .toList();

        if (categoryIds.isEmpty()) {
            return Page.empty(pageable);
        }

        int perCategory = Math.max(1, size / categoryIds.size());
        List<News> balanced = new ArrayList<>();

        for (Long categoryId : categoryIds) {
            Pageable categoryPage = PageRequest.of(page, perCategory);
            List<News> news;

            if (after != null) {
                news = newsRepository
                        .findByCategoryIdsAndAfter(List.of(categoryId), after, categoryPage)
                        .getContent();
            } else {
                news = newsRepository
                        .findByCategoryIdInAndActiveTrueOrderByPublishedAtDesc(List.of(categoryId), categoryPage)
                        .getContent();
            }

            balanced.addAll(news);
        }

        if (balanced.size() > size) {
            balanced = balanced.subList(0, size);
        }

        long totalCount = after != null
                ? newsRepository.findByCategoryIdsAndAfter(categoryIds, after, pageable).getTotalElements()
                : newsRepository.findByCategoryIdInAndActiveTrueOrderByPublishedAtDesc(categoryIds, pageable).getTotalElements();

        return new PageImpl<>(balanced, pageable, totalCount);
    }

    /* ============================================================
       GET SINGLE NEWS (INCREMENT VIEW COUNT)
       ============================================================ */

    public NewsDTO getNewsById(Long id) {

        News news = newsRepository.findById(id)
                .filter(News::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("News not found"));

        newsRepository.incrementViewCount(id);

        return mapToDTO(news);
    }

    /* ============================================================
       CREATE NEWS — AI summarization happens here, once at insert time
       ============================================================ */

    public NewsDTO createNews(NewsCreateRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Use `description` if `content` is missing/short (common with news APIs)
        String rawContent = (request.getContent() != null && request.getContent().length() > 100)
                ? request.getContent()
                : (request.getDescription() != null ? request.getDescription() : request.getContent());

        // AI generates shortHeadline + shortContent in one call
        AiSummarizationService.SummaryResult summary =
                aiSummarizationService.summarize(request.getTitle(), rawContent);

        News news = new News();
        news.setCategory(category);
        news.setTitle(request.getTitle());
        news.setContent(rawContent);
        news.setShortHeadline(summary.shortHeadline());
        news.setShortContent(summary.shortContent());
        news.setImageUrl(request.getImageUrl());
        news.setSource(request.getSource() != null ? request.getSource() : request.getSourceName());
        news.setPublishedAt(LocalDateTime.now(ZoneOffset.UTC));

        news = newsRepository.save(news);

        return mapToDTO(news);
    }

    /* ============================================================
       DTO MAPPING
       ============================================================ */

    private NewsDTO mapToDTO(News news) {
        return new NewsDTO(
                news.getId(),
                news.getCategory().getId(),
                news.getCategory().getName(),
                news.getShortHeadline(),
                news.getShortContent(),
                news.getImageUrl(),
                news.getSource(),
                news.getViewCount(),
                news.getPublishedAt(),
                news.getCreatedAt(),
                news.getUpdatedAt()
        );
    }
}