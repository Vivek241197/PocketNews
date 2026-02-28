package com.pocketnews.repository;

import com.pocketnews.entity.ArticleTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleTranslationRepository extends JpaRepository<ArticleTranslation, Long> {
    Optional<ArticleTranslation> findByArticleIdAndLanguageCode(Long articleId, String languageCode);
}
