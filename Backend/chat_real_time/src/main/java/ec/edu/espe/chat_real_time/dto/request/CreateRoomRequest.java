package ec.edu.espe.chat_real_time.dto.request;

import ec.edu.espe.chat_real_time.model.room.RoomType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {
  @NotBlank(message = "El nombre de la sala es requerido")
  @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
  private String name;

  @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
  private String description;

  @NotBlank(message = "El PIN es requerido")
  @Size(min = 4, max = 10, message = "El PIN debe tener entre 4 y 10 caracteres")
  @Pattern(regexp = "^[0-9]+$", message = "El PIN debe contener solo números")
  private String pin;

  @NotNull(message = "El tipo de sala es requerido")
  private RoomType type;

  @Min(value = 2, message = "La sala debe permitir al menos 2 usuarios")
  @Max(value = 100, message = "La sala no puede exceder 100 usuarios")
  private Integer maxUsers = 50;

  @Min(value = 1, message = "El tamaño máximo de archivo debe ser al menos 1MB")
  @Max(value = 50, message = "El tamaño máximo de archivo no puede exceder 50MB")
  private Integer maxFileSizeMb = 10;
}