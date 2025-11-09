package ec.edu.espe.chat_real_time.dto.mapperDTO;

import ec.edu.espe.chat_real_time.dto.response.AttachmentResponse;
import ec.edu.espe.chat_real_time.dto.response.MessageResponse;
import ec.edu.espe.chat_real_time.model.message.Message;

import java.util.List;
import java.util.stream.Collectors;

public class MessageMapper {
  public static MessageResponse toMessageResponse(Message message) {
    List<AttachmentResponse> attachments = message.getAttachments().stream()
            .map(att -> AttachmentResponse.builder()
                    .id(att.getId())
                    .fileName(att.getFileName())
                    .originalFileName(att.getOriginalFileName())
                    .fileType(att.getFileType())
                    .fileSize(att.getFileSize())
                    .fileUrl(att.getFileUrl())
                    .uploadedAt(att.getUploadedAt())
                    .build())
            .collect(Collectors.toList());

    return MessageResponse.builder()
            .id(message.getId())
            .content(message.getContent())
            .messageType(message.getMessageType())
            .sentAt(message.getSentAt())
            .isEdited(message.getIsEdited())
            .editedAt(message.getEditedAt())
            .senderNickname(message.getSession().getNicknameInRoom())
            .senderId(message.getUser().getId())
            .roomId(message.getRoom().getId())
            .attachments(attachments)
            .build();
  }
}
