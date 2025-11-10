package ec.edu.espe.chat_real_time.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// DTOs internos
@lombok.Data
@lombok.Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserJoinedMessage {
  private Long userId;
  private String username;
  private String action;
  private LocalDateTime timestamp;
}