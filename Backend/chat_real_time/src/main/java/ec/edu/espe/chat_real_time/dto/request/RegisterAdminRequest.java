package ec.edu.espe.chat_real_time.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterAdminRequest {
  @NotBlank
  @Size(min = 3, max = 50)
  private String username;

  @NotBlank
  @Size(min = 6, max = 100)
  private String password;

  @NotBlank
  @Size(min = 1, max = 100)
  private String firstName;

  @NotBlank
  @Size(min = 1, max = 100)
  private String lastName;

  @NotBlank
  @Email
  private String email;

  @Size(max = 20)
  private String phone;
}
