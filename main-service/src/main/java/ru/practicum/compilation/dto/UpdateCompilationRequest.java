package ru.practicum.compilation.dto;

import lombok.Data;
import ru.practicum.compilation.exceptions.CompilationValidation;

import java.util.List;

@Data
public class UpdateCompilationRequest {
    private final String title;
    private final Boolean pinned;
    private final List<Long> events;

    public UpdateCompilationRequest(String title, Boolean pinned, List<Long> events) {
        if (title != null && (title.isEmpty() || title.length() > 50)) {
            throw new CompilationValidation("размер title должен находиться в диапазоне от 1 до 50");
        }

        this.title = title;
        this.pinned = pinned;
        this.events = events;
    }
}
