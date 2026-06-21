package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.service.CategoryService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/categories")
public class AdminCategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody NewCategoryDto dto) {
        log.info("POST /admin/categories - Create category with name: {}", dto.getName());
        return categoryService.createCategory(dto);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(
            @PathVariable Long catId,
            @Valid @RequestBody CategoryDto dto) {
        log.info("PATCH /admin/categories/{} - Update category with id: {}, new name: {}", catId, catId, dto.getName());
        return categoryService.updateCategory(catId, dto);

    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("DELETE /admin/categories/{} - Delete category with id: {}", catId, catId);
        categoryService.deleteCategory(catId);
    }
}