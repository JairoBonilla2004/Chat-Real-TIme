package ec.edu.espe.chat_real_time.dto.mapperDTO;

import ec.edu.espe.chat_real_time.dto.response.AttachmentResponse;
import ec.edu.espe.chat_real_time.dto.response.MessageResponse;
import ec.edu.espe.chat_real_time.model.message.Message;
import ec.edu.espe.chat_real_time.model.user.User;

import java.util.List;
import java.util.stream.Collectors;

public class MessageMapper {

  public static MessageResponse toMessageResponse(Message message) {
    User user = message.getUser();
    String roleName = user.getRoles().stream()
            .findFirst()
            .map(role -> role.getName())
            .orElse("ROLE_GUEST");

    String senderNickname = roleName.equals("ROLE_ADMIN")
            ? user.getAdminProfile().getFirstName() + " " + user.getAdminProfile().getLastName() + " (Admin)"
            : user.getGuestProfile().getNickname();

    boolean isDeleted = Boolean.TRUE.equals(message.getIsDeleted());

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
            .content(isDeleted ? "" : message.getContent())
            .messageType(message.getMessageType())
            .sentAt(message.getSentAt())
            .isEdited(message.getIsEdited())
            .editedAt(message.getEditedAt())
            .isDeleted(isDeleted)
            .senderNickname(senderNickname)
            .senderId(user.getId())
            .roomId(message.getRoom().getId())
            .attachments(isDeleted ? List.of() : attachments)
            .build();
  }
}
