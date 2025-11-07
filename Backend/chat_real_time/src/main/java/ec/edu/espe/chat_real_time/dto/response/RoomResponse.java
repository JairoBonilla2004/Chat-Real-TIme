package ec.edu.espe.chat_real_time.dto.response;

import ec.edu.espe.chat_real_time.model.room.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {
  private Long id;
  private String roomCode;
  private String name;
  private String description;
  private RoomType type;
  private Integer maxUsers;
  private Integer currentUsers;
  private Integer maxFileSizeMb;
  private Boolean isActive;
  private Boolean isFull;
  private LocalDateTime createdAt;
  private UserAdminResponse creator;
}