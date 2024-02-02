package ru.practicum.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@AllArgsConstructor
public class NewCompilationDto {
    @NotBlank
    @NotNull
    @Size(min = 1, max = 50)
    private final String title;

    private Boolean pinned;
    private List<Long> events;
}
