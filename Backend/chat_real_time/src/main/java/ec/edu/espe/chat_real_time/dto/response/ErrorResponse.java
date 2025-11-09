package ec.edu.espe.chat_real_time.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
  private Boolean success;
  private String message;
  private String error;
  private Integer status;
  private String path;
  private LocalDateTime timestamp;
  private List<String> details;
}
