package com.pocketnews.service;

import com.pocketnews.dto.CategoryDTO;
import com.pocketnews.dto.CategoryCreateRequest;
import com.pocketnews.entity.Category;
import com.pocketnews.exception.ResourceNotFoundException;
import com.pocketnews.exception.BadRequestException;
import com.pocketnews.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public Page<CategoryDTO> getAllCategories(Pageable pageable) {
        Page<Category> categories = categoryRepository.findAll(pageable);
        List<CategoryDTO> dtos = categories.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, categories.getTotalElements());
    }

    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return mapToDTO(category);
    }

    public CategoryDTO createCategory(CategoryCreateRequest request) {
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Category with this slug already exists");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setDescription(request.getDescription());
        category.setIconUrl(request.getIconUrl());
        category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);

        category = categoryRepository.save(category);
        return mapToDTO(category);
    }

    public CategoryDTO updateCategory(Long id, CategoryCreateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIconUrl(request.getIconUrl());
        category.setDisplayOrder(request.getDisplayOrder());

        category = categoryRepository.save(category);
        return mapToDTO(category);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        categoryRepository.delete(category);
    }

    private CategoryDTO mapToDTO(Category category) {
        return new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getIconUrl(),
                category.getDisplayOrder(),
                category.getIsActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}

