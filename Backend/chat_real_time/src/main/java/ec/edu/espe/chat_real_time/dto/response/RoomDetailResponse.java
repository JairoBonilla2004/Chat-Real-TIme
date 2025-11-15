package ec.edu.espe.chat_real_time.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDetailResponse {
  private RoomResponse room;
  private List<SessionResponse> activeSessions;
  private List<MessageResponse> recentMessages;
  private Integer activeUsersCount;
  private String token;
}