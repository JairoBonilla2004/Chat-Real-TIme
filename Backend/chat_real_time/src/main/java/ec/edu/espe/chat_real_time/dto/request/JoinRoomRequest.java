package ec.edu.espe.chat_real_time.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinRoomRequest {
  @NotBlank(message = "El código de sala es requerido")
  private String roomCode;
  @NotBlank(message = "El PIN es requerido")
  private String pin;// el pin es

  private String deviceId; // Identificador único del dispositivo del usuario sirve para gestionar sesiones y conexiones
}