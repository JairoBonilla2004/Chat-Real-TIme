package ec.edu.espe.chat_real_time.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
  private String accessToken;
  private String refreshToken;
  private String tokenType = "Bearer";
  private Long expiresIn;
  private UserResponse user;
}
