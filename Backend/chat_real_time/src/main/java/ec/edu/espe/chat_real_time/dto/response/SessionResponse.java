package ec.edu.espe.chat_real_time.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
  private Long id;
  private String nicknameInRoom;
  private LocalDateTime joinedAt;
  private Boolean isActive;
  private String ipAddress;
}