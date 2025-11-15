package ec.edu.espe.chat_real_time.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class RegisterRequest
{
    @NotBlank(message = "El user name es requerido")
    @Size(min = 5, max = 50, message = "El nombre debe tener entre 5 a 50 caracteres")
    @Pattern(regexp = "^[A-Za-zÑñ ]+$", message = "El user name solo debe contener letras")
    private String username;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, max = 100, message = "La contraseña debe tener mínimo 6 caracteres")
    private String password;

    @NotBlank(message = "El nombre es requerido")
    @Pattern(regexp = "^[A-Za-zÑñ ]+$", message = "El nombre solo debe contener letras")
    private String firstName;

    @NotBlank(message = "El apellido es requerido")
    @Pattern(regexp = "^[A-Za-zÑñ ]+$", message = "El apellido solo debe contener letras")
    private String lastName;

    @NotBlank(message = "El email es requerido")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "El número de teléfono es requerido")
    @Pattern(regexp = "^[0-9]+$", message = "El teléfono solo debe contener números")
    @Size(min = 10, max = 10, message = "El teléfono debe contener exactamente 10 dígitos")
    private String phone;
}

