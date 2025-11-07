package ec.edu.espe.chat_real_time.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {
  private Long id;
  private String fileName;
  private String originalFileName;
  private String fileType;
  private Long fileSize;
  private String fileUrl;
  private LocalDateTime uploadedAt;
}