package ec.edu.espe.chat_real_time.Service.message;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import ec.edu.espe.chat_real_time.dto.mapperDTO.MessageMapper;
import ec.edu.espe.chat_real_time.dto.request.SendMessageRequest;
import ec.edu.espe.chat_real_time.dto.response.MessageResponse;
import ec.edu.espe.chat_real_time.exception.BadRequestException;
import ec.edu.espe.chat_real_time.exception.ResourceNotFoundException;
import ec.edu.espe.chat_real_time.exception.UnauthorizedException;
import ec.edu.espe.chat_real_time.model.Attachment;
import ec.edu.espe.chat_real_time.model.message.Message;
import ec.edu.espe.chat_real_time.model.message.MessageType;
import ec.edu.espe.chat_real_time.model.room.Room;
import ec.edu.espe.chat_real_time.model.room.RoomType;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.model.user.UserSession;
import ec.edu.espe.chat_real_time.repository.AttachmentRepository;
import ec.edu.espe.chat_real_time.repository.MessageRepository;
import ec.edu.espe.chat_real_time.repository.RoomRepository;
import ec.edu.espe.chat_real_time.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

  private final MessageRepository messageRepository;
  private final RoomRepository roomRepository;
  private final UserSessionRepository sessionRepository;
  private final AttachmentRepository attachmentRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final Cloudinary cloudinary;


  @Override
  @Transactional
  public MessageResponse sendTextMessage(SendMessageRequest request, User user) {
    log.info("User {} sending text message to room {}", user.getUsername(), request.getRoomId());

    Room room = roomRepository.findByIdAndDeletedAtIsNull(request.getRoomId())
            .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada"));

    UserSession session = sessionRepository.findByUserAndRoomAndIsActiveTrue(user, room)
            .orElseThrow(() -> new BadRequestException("No estás conectado a esta sala"));

    Message message = Message.builder()
            .content(request.getContent())
            .messageType(MessageType.TEXT)
            .user(user)
            .room(room)
            .session(session)
            .isEdited(false)
            .isDeleted(false)
            .build();

    message = messageRepository.save(message);
    log.info("Text message sent successfully: {}", message.getId());

    MessageResponse response = MessageMapper.toMessageResponse(message);
    messagingTemplate.convertAndSend( //convertAndSend envia el mensaje a todos los que estan suscritos al topic
            "/topic/room/" + room.getId(),
            response
    );

    return response;
  }

  @Override
  @Transactional
  public MessageResponse sendFileMessage(Long roomId, String content, MultipartFile file, User user) {
    log.info("User {} sending file message to room {}", user.getUsername(), roomId);

    Room room = roomRepository.findByIdAndDeletedAtIsNull(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada"));

    if (room.getType() != RoomType.MULTIMEDIA) {
      throw new BadRequestException("Esta sala no permite archivos");
    }

    UserSession session = sessionRepository.findByUserAndRoomAndIsActiveTrue(user, room)
            .orElseThrow(() -> new BadRequestException("No estás conectado a esta sala"));

    if (file.isEmpty()) {
      throw new BadRequestException("El archivo está vacío");
    }

    long fileSizeInMb = file.getSize() / (1024 * 1024);
    if (fileSizeInMb > room.getMaxFileSizeMb()) {
      throw new BadRequestException(
              String.format("El archivo excede el tamaño máximo permitido de %dMB", room.getMaxFileSizeMb())
      );
    }

    // Crear el mensaje base
    Message message = Message.builder()
            .content(content != null ? content : "Archivo adjunto")
            .messageType(MessageType.FILE)
            .user(user)
            .room(room)
            .session(session)
            .isEdited(false)
            .isDeleted(false)
            .build();

    message = messageRepository.save(message);

    try {
      String originalFilename = file.getOriginalFilename();
      String mimeType = file.getContentType();

      // Detectar si es image, video o raw (PDF, ZIP, DOCX...)
      String resourceType = getResourceType(mimeType);

      log.info("Detected resourceType: {} for mimeType: {}", resourceType, mimeType);

      @SuppressWarnings("unchecked")
      var uploadResult = cloudinary.uploader().upload(
              file.getBytes(),
              ObjectUtils.asMap(
                      "resource_type", resourceType,
                      "folder", "chat_real_time"
              )
      );

      log.info("Cloudinary upload result: {}", uploadResult);

      String secureUrl = (String) uploadResult.get("secure_url");
      String publicId = (String) uploadResult.get("public_id");
      String cloudinaryFormat = (String) uploadResult.get("format");

      String extension = "";
      if (originalFilename != null && originalFilename.contains(".")) {
        extension = originalFilename.substring(originalFilename.lastIndexOf(".")); // ej: .pdf .zip .docx
      }
      if (cloudinaryFormat != null && !cloudinaryFormat.isBlank()) {
        extension = "." + cloudinaryFormat;  // ej: .png .jpg
      }
      String storedFileName = publicId + extension;

      Attachment attachment = Attachment.builder()
              .fileName(storedFileName)
              .originalFileName(originalFilename)
              .fileType(mimeType)
              .fileSize(file.getSize())
              .filePath(publicId)
              .fileUrl(secureUrl)
              .message(message)
              .build();

      attachmentRepository.save(attachment);
      log.info("File saved successfully: {}", publicId);

    } catch (IOException e) {
      log.error("Error uploading file to Cloudinary", e);
      throw new BadRequestException("Error al subir el archivo");
    }

    MessageResponse response = MessageMapper.toMessageResponse(message);

    messagingTemplate.convertAndSend(
            "/topic/room/" + room.getId(),
            response
    );

    return response;
  }


  private String getResourceType(String mimeType) {
    if (mimeType == null) return "raw";

    if (mimeType.startsWith("image/")) return "image";
    if (mimeType.startsWith("video/")) return "video";
    return "raw";
  }


  @Override
  @Transactional(readOnly = true)
  public List<MessageResponse> getRoomMessages(Long roomId, User user) {
    Room room = roomRepository.findByIdAndDeletedAtIsNull(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada"));

    sessionRepository.findByUserAndRoomAndIsActiveTrue(user, room)
            .orElseThrow(() -> new UnauthorizedException("No estás conectado a esta sala"));

    return messageRepository.findByRoomAndIsDeletedFalseOrderBySentAtDesc(room)
            .stream()
            .map(MessageMapper::toMessageResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public MessageResponse getMessageById(Long messageId) {
    Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("Mensaje no encontrado"));

    return MessageMapper.toMessageResponse(message);
  }

  @Override
  @Transactional
  public void deleteMessage(Long messageId, User user) {
    Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("Mensaje no encontrado"));

    if (!message.getUser().getId().equals(user.getId())) {
      throw new UnauthorizedException("No tienes permiso para eliminar este mensaje");
    }

    message.setIsDeleted(true);
    message.setDeletedAt(LocalDateTime.now());
    messageRepository.save(message);


    messagingTemplate.convertAndSend(
            "/topic/room/" + message.getRoom().getId(),
            MessageMapper.toMessageResponse(message)
    );

    log.info("Message {} deleted by user {}", messageId, user.getUsername());
  }
}