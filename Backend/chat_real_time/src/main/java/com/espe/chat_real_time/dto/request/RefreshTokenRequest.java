package com.espe.chat_real_time.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {
  @NotBlank(message = "El refresh token es requerido")
  private String refreshToken;
}