package com.espe.chat_real_time.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuestLoginRequest {
  @NotBlank(message = "El nickname es requerido")
  @Size(min = 3, max = 50, message = "El nickname debe tener entre 3 y 50 caracteres")
  private String nickname;
}