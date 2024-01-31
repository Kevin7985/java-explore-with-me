package ru.practicum.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class NewUserRequest {
    @NotNull
    @NotBlank
    @Email
    @Size(min = 6, max = 254)
    private final String email;

    @NotNull
    @NotBlank
    @Size(min = 2, max = 250)
    private final String name;
}
