package ru.practicum.сategory;

import org.springframework.data.repository.PagingAndSortingRepository;
import ru.practicum.сategory.model.Category;

public interface CategoryRepository extends PagingAndSortingRepository<Category, Long> { }
