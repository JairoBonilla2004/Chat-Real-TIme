package ec.edu.espe.chat_real_time.dto.response;

import ec.edu.espe.chat_real_time.model.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
  private Long id;
  private String username;
  private String email;
  private UserRole role;
  private String nickname;
  private Boolean isGuest;
  private LocalDateTime guestExpiresAt;
  private Boolean isActive;
  private LocalDateTime createdAt;
}