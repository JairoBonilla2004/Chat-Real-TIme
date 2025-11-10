package ec.edu.espe.chat_real_time.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusMessage {
  private Long userId;
  private String username;
  private String status; // e.g., "ONLINE" or "OFFLINE"
}
