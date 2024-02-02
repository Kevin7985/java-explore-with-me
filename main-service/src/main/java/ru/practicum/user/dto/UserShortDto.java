package ru.practicum.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class UserShortDto {
    private final Long id;

    @NotBlank
    @NotNull
    private final String name;
}
