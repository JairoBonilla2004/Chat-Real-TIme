package ec.edu.espe.chat_real_time.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@lombok.Data
@lombok.Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorMessage {
  private String message;
  private LocalDateTime timestamp;
}
