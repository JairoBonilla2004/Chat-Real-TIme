package ec.edu.espe.chat_real_time.dto.response;

import ec.edu.espe.chat_real_time.model.message.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
  private Long id;
  private String content;
  private MessageType messageType;
  private LocalDateTime sentAt;
  private Boolean isEdited;
  private LocalDateTime editedAt;
  private String senderNickname;
  private Long senderId;
  private Long roomId;
  private List<AttachmentResponse> attachments;
}