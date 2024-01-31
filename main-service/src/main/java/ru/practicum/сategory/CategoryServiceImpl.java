package ru.practicum.сategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.service.MapperService;
import ru.practicum.service.ValidationService;
import ru.practicum.utils.Pagination;
import ru.practicum.сategory.dto.CategoryDto;
import ru.practicum.сategory.dto.NewCategoryDto;
import ru.practicum.сategory.model.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final MapperService mapperService;
    private final ValidationService validationService;
    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Category newCategory = mapperService.toCategory(newCategoryDto);

        log.info("Добавлена новая категория: " + newCategory);
        return mapperService.toCategoryDto(categoryRepository.save(newCategory));
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        Category category = validationService.validateCategory(catId);

        log.info("Получена информация о категории с id = " + catId);
        return mapperService.toCategoryDto(category);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        List<CategoryDto> categories = new ArrayList<>();

        Pageable pageable;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Page<Category> page;
        Pagination pager = new Pagination(from, size);

        if (size == null) {
            pageable = PageRequest.of(pager.getPageStart(), pager.getPageSize(), sort);
            page = categoryRepository.findAll(pageable);

            while (page.hasContent()) {
                categories.addAll(page.stream()
                        .map(mapperService::toCategoryDto)
                        .collect(Collectors.toList()));
                pageable = pageable.next();
                page = categoryRepository.findAll(pageable);
            }
        } else {
            for (int i = pager.getPageStart(); i < pager.getPagesAmount(); i++) {
                pageable = PageRequest.of(i, pager.getPageSize(), sort);
                page = categoryRepository.findAll(pageable);
                categories.addAll(page.stream()
                        .map(mapperService::toCategoryDto)
                        .collect(Collectors.toList()));
            }

            categories = categories.stream().limit(size).collect(Collectors.toList());
        }

        log.info("Получен список всех категорий");
        return categories;
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        Category category = validationService.validateCategory(catId);

        category.setName(categoryDto.getName());
        log.info("Обновлена категория: " + category);
        return mapperService.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long catId) {
        validationService.validateCategory(catId);

        log.info("Удалена категория с id = " + catId);
        categoryRepository.deleteById(catId);
    }
}
