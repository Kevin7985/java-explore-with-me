package ru.practicum.category.dto;

import org.springframework.stereotype.Component;
import ru.practicum.category.model.Category;

@Component
public class CategoryMapper {
    public Category toCategory(NewCategoryDto categoryDto) {
        return new Category(
                null,
                categoryDto.getName()
        );
    }

    public CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName()
        );
    }
}
