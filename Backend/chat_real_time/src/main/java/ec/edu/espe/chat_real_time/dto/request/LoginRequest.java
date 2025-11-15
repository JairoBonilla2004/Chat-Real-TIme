package ec.edu.espe.chat_real_time.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
  @NotBlank(message = "El nombre de usuario es requerido")
  @Pattern(regexp = "^[A-Za-zÑñ ]+$", message = "El user name solo debe contener letras")
  private String username;

  @NotBlank(message = "La contraseña es requerida")
  private String password;
}