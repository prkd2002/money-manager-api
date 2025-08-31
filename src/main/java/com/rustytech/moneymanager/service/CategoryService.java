package com.rustytech.moneymanager.service;

import com.rustytech.moneymanager.dtos.CategoryDto;
import com.rustytech.moneymanager.entity.CategoryEntity;
import com.rustytech.moneymanager.entity.Profile;
import com.rustytech.moneymanager.exceptions.CategoryNotFoundException;
import com.rustytech.moneymanager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;

    public CategoryDto saveCategory(CategoryDto categoryDto) {
        Profile profile = profileService.getCurrentProfile();
        if(categoryRepository.existsByNameAndProfileId(categoryDto.getName(), profile.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name already exists");
        }
        CategoryEntity categoryEntity = toEntity(categoryDto, profile);
        categoryEntity = categoryRepository.save(categoryEntity);
        return toDto(categoryEntity);
    }

    public List<CategoryDto> getCategoriesForCurrentUser(){
        Profile profile = profileService.getCurrentProfile();
        List<CategoryEntity> categories = categoryRepository.findByProfileId(profile.getId());
        return categories.stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<CategoryDto> getCategoriesByTypeForCurrentUser(String type) {
        Profile profile = profileService.getCurrentProfile();
        List<CategoryEntity> categories = categoryRepository.findByTypeAndProfileId(type.substring(0,1).toUpperCase() + type.substring(1), profile.getId());
        return categories.stream().map(this::toDto).collect(Collectors.toList());
    }

    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Profile profile = profileService.getCurrentProfile();
        CategoryEntity existingCategory = categoryRepository.findByIdAndProfileId(id,profile.getId()).orElseThrow(() -> new CategoryNotFoundException("Category not found or not accessible"));
        existingCategory.setName(categoryDto.getName());
        existingCategory.setIcon(categoryDto.getIcon());
        existingCategory.setType(categoryDto.getType());
        existingCategory = categoryRepository.save(existingCategory);
        return toDto(existingCategory);
    }

    private CategoryEntity toEntity(CategoryDto category, Profile profile) {
        return CategoryEntity.builder().id(category.getId())
                .name(category.getName())
                .icon(category.getIcon())
                .type(category.getType())
                .profile(profile)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }


    private CategoryDto toDto(CategoryEntity category) {
        return CategoryDto.builder()
                .id(category.getId())
                .icon(category.getIcon())
                .profileId(category.getProfile() == null ? null : category.getProfile().getId())
                .type(category.getType())
                .name(category.getName())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();

    }
}
