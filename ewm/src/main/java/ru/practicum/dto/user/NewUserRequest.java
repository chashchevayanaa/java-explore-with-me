package ru.practicum.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewUserRequest {
    @NotBlank
    @Size(min = 2, max = 250, message = "Field: name. Error: must be between 2 and 250 characters. Value: ${validatedValue}\"")
    private String name;

    @NotBlank
    @Email
    @Size(min = 6, max = 254, message = "Field: email. Error: must be between 6 and 254 characters. Value: ${validatedValue}\"")
    private String email;

}
