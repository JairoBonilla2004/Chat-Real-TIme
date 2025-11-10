package ec.edu.espe.chat_real_time.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMessage {
  private String content;
  private String type; // SYSTEM, INFO, WARNING
  private LocalDateTime timestamp;
}