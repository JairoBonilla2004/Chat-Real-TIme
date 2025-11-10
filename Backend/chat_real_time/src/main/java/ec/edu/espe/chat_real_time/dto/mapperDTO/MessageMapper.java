package ec.edu.espe.chat_real_time.dto.mapperDTO;

import ec.edu.espe.chat_real_time.dto.response.AttachmentResponse;
import ec.edu.espe.chat_real_time.dto.response.MessageResponse;
import ec.edu.espe.chat_real_time.model.Role;
import ec.edu.espe.chat_real_time.model.message.Message;
import ec.edu.espe.chat_real_time.model.user.User;

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

    String senderNickname;
    User user = message.getUser();
    String role_user = user.getRoles().stream().findFirst().get().getName();
    if (role_user.equals("ROLE_ADMIN")) {
      senderNickname = user.getAdminProfile().getFirstName() + " " + user.getAdminProfile().getLastName() + " (Admin)";
    } else {
      senderNickname = user.getGuestProfile().getNickname();
    }

    return MessageResponse.builder()
            .id(message.getId())
            .content(message.getContent())
            .messageType(message.getMessageType())
            .sentAt(message.getSentAt())
            .isEdited(message.getIsEdited())
            .editedAt(message.getEditedAt())
            .senderNickname(senderNickname)
            .senderId(message.getUser().getId())
            .roomId(message.getRoom().getId())
            .attachments(attachments)
            .build();
  }
}
