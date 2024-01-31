package ru.practicum.сategory.dto;

import org.springframework.stereotype.Component;
import ru.practicum.сategory.model.Category;

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
