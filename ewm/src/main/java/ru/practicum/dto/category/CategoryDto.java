package ru.practicum.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private Long id;
    @NotBlank
    @Size(min = 1, max = 50, message = "Field: name. Error: must be between 1 and 50 characters. Value: ${validatedValue}")
    private String name;
}