package com.espe.chat_real_time.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
  @NotBlank(message = "El nombre de usuario es requerido")
  @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
  private String username;

  @NotBlank(message = "El email es requerido")
  @Email(message = "El email debe ser válido")
  private String email;

  @NotBlank(message = "La contraseña es requerida")
  @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
  private String password;

  @Size(max = 50, message = "El nickname no puede exceder 50 caracteres")
  private String nickname;
}