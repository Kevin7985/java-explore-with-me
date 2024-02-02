package ru.practicum.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {
    private final Long id;
    private final String email;
    private final String name;
}
