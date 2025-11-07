package com.espe.chat_real_time.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
  @NotNull(message = "El ID de la sala es requerido")
  private Long roomId;

  @NotBlank(message = "El contenido del mensaje es requerido")
  private String content;
}