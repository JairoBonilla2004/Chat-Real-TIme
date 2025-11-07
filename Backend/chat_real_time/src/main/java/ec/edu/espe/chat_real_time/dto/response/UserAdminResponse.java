package ec.edu.espe.chat_real_time.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAdminResponse {
  private Long id;
  private String name;
  private String lastName;
  private String email;
  private String username;
  private String phone;
  private String role;
  private boolean enabled;
}