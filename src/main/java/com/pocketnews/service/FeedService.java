package com.pocketnews.service;

import com.pocketnews.dto.ArticleDTO;
import com.pocketnews.entity.Article;
import com.pocketnews.entity.ArticleTranslation;
import com.pocketnews.entity.UserProfile;
import com.pocketnews.exception.BadRequestException;
import com.pocketnews.repository.ArticleRepository;
import com.pocketnews.repository.ArticleTranslationRepository;
import com.pocketnews.repository.UserProfileRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
    public class FeedService {

        private final ArticleRepository articleRepository;
        private final ArticleTranslationRepository translationRepository;
        private final TranslationService translationService;
        private final UserProfileRepository userProfileRepository;

        // constructor injection...
        public FeedService(ArticleRepository articleRepository,ArticleTranslationRepository translationRepository,
                           TranslationService translationService,UserProfileRepository userProfileRepository){
            this.articleRepository=articleRepository;
            this.translationRepository=translationRepository;
            this.translationService=translationService;
            this.userProfileRepository=userProfileRepository;
        }
        public List<ArticleDTO> getFeed(String deviceId, int page, int size) {

            // Step 1: Get user's language
            UserProfile profile = userProfileRepository
                    .findByDeviceId(deviceId)
                    .orElseThrow(() -> new BadRequestException("User not onboarded"));

            String languageCode = profile.getLanguageCode();

            // Step 2: Fetch English articles (paginated)
            Pageable pageable = PageRequest.of(page, size);
            List<Article> articles = articleRepository.findAllByOrderByPublishedAtDesc(pageable);

            // Step 3: For each article, check cache → translate if missing
            return articles.stream()
                    .map(article -> resolveTranslation(article, languageCode))
                    .collect(Collectors.toList());
        }

        private ArticleDTO resolveTranslation(Article article, String languageCode) {

            // English — no translation needed
            if ("en".equals(languageCode)) {
                return mapToDTO(article, article.getTitle(), article.getSummary());
            }

            // Check if translation already exists in DB (cache)
            Optional<ArticleTranslation> cached = translationRepository
                    .findByArticleIdAndLanguageCode(article.getId(), languageCode);

            if (cached.isPresent()) {
                // Cache hit — serve from DB
                return mapToDTO(article, cached.get().getTitle(), cached.get().getSummary());
            }

            // Cache miss — call Google Translate
            String translatedTitle = translationService.translate(article.getTitle(), languageCode);
            String translatedSummary = translationService.translate(article.getSummary(), languageCode);

            // Store translation in DB for future requests
            ArticleTranslation translation = new ArticleTranslation();
            translation.setArticle(article);
            translation.setLanguageCode(languageCode);
            translation.setTitle(translatedTitle);
            translation.setSummary(translatedSummary);
            translationRepository.save(translation);

            return mapToDTO(article, translatedTitle, translatedSummary);
        }

        private ArticleDTO mapToDTO(Article article, String title, String summary) {
            return new ArticleDTO(
                    article.getId(),
                    title,
                    summary,
                    article.getImageUrl(),
                    article.getSourceUrl(),
                    article.getSourceName(),
                    article.getCategory(),
                    article.getPublishedAt()
            );
        }
    }