package ru.practicum.category.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class CategoryDto {
    private final Long id;

    @NotNull
    @NotBlank
    @Size(min = 1, max = 50)
    private final String name;
}
