package ec.edu.espe.chat_real_time.dto.response;
import lombok.Builder;
import lombok.Data;
@Data
@Builder

public class RegisterResponse {
    private Long id;
    private String username;
    private String role;

}
