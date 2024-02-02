package ru.practicum.category;

import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.category.model.Category;

public interface CategoryRepository extends PagingAndSortingRepository<Category, Long> { }
