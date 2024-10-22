package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NewEventDto {
    @NotNull
    @NotBlank
    @Size(min = 3, max = 120)
    private final String title;

    @NotNull
    @NotBlank
    @Size(min = 20, max = 2000)
    private final String annotation;

    @NotNull
    @NotBlank
    @Size(min = 20, max = 7000)
    private final String description;

    @JsonProperty("category")
    private final Long categoryId;

    private final LocalDateTime eventDate;
    private Boolean paid;

    @PositiveOrZero
    private Integer participantLimit;
    private Boolean requestModeration;
    private Location location;
}
