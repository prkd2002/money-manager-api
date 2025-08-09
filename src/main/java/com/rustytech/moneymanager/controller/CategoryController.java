package com.rustytech.moneymanager.controller;

import com.rustytech.moneymanager.dtos.CategoryDto;
import com.rustytech.moneymanager.exceptions.CategoryNotFoundException;
import com.rustytech.moneymanager.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> saveCategory(@RequestBody CategoryDto category) {
        try{
            CategoryDto categoryDto = categoryService.saveCategory(category);
            return new ResponseEntity<>(categoryDto, HttpStatus.CREATED);
        }catch (ResponseStatusException e){
           throw new ResponseStatusException(HttpStatus.CONFLICT, "Category with name " + category.getName() + " already exists");
        }
    }


    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getCategoriesForCurrentUser());
    }

    @GetMapping("/{type}")
    public ResponseEntity<List<CategoryDto>> getCategoriesByTypeForCurrentUser(@PathVariable String type) {
        return ResponseEntity.ok(categoryService.getCategoriesByTypeForCurrentUser(type));
    }

    @PutMapping("/{category_id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Long category_id,@RequestBody CategoryDto categoryDto) {
        try{
            CategoryDto category = categoryService.updateCategory(category_id, categoryDto);
            return new ResponseEntity<>(category, HttpStatus.OK);

        }catch (CategoryNotFoundException e){
            throw new CategoryNotFoundException( e.getMessage() );
        }

    }
}
