package ec.edu.espe.chat_real_time.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingIndicator {
  private Long userId;
  private String username;
  private Long roomId;
  private boolean isTyping;
}