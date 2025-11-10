package ec.edu.espe.chat_real_time.Service.websocket;


import ec.edu.espe.chat_real_time.Service.message.MessageService;
import ec.edu.espe.chat_real_time.dto.request.SendMessageRequest;
import ec.edu.espe.chat_real_time.dto.response.MessageResponse;
import ec.edu.espe.chat_real_time.dto.websocket.*;
import ec.edu.espe.chat_real_time.model.user.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketServiceImpl implements WebSocketService {

  private final SimpMessagingTemplate messagingTemplate;
  private final MessageService messageService;

  @Override
  @Transactional
  public MessageResponse sendMessageToRoom(SendMessageRequest request, User user) {
    log.info("WebSocket: User {} sending message to room {}", user.getUsername(), request.getRoomId());

    try {
      // Guardar mensaje en BD usando el servicio existente
      MessageResponse message = messageService.sendTextMessage(request, user);

      // Enviar via WebSocket a todos los usuarios de la sala
      messagingTemplate.convertAndSend(
              "/topic/room/" + request.getRoomId(),
              message
      );

      log.info("WebSocket: Message sent successfully to room {}", request.getRoomId());
      return message;

    } catch (Exception e) {
      log.error("WebSocket: Error sending message to room {}", request.getRoomId(), e);
      sendErrorToUser(user.getUsername(), "Error al enviar mensaje: " + e.getMessage());
      throw e;
    }
  }

  @Override
  public void sendTypingIndicator(Long roomId, TypingIndicator typingIndicator, User user) {
    log.debug("WebSocket: Typing indicator for user {} in room {}", user.getUsername(), roomId);

    typingIndicator.setUserId(user.getId());
    typingIndicator.setUsername(user.getUsername());
    typingIndicator.setRoomId(roomId);

    messagingTemplate.convertAndSend(
            "/topic/room/" + roomId + "/typing",
            typingIndicator
    );
  }

  @Override
  public void notifyUserJoinedRoom(Long roomId, User user) {
    log.info("WebSocket: Notifying user {} joined room {}", user.getUsername(), roomId);

    UserJoinedMessage message = UserJoinedMessage.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .action("JOINED")
            .timestamp(LocalDateTime.now())
            .build();

    messagingTemplate.convertAndSend(
            "/topic/room/" + roomId + "/users",
            message
    );

    // También enviar mensaje de sistema
    sendSystemMessageToRoom(roomId, user.getUsername() + " se ha unido a la sala");
  }

  @Override
  public void notifyUserLeftRoom(Long roomId, User user) {
    log.info("WebSocket: Notifying user {} left room {}", user.getUsername(), roomId);

    UserJoinedMessage message = UserJoinedMessage.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .action("LEFT")
            .timestamp(LocalDateTime.now())
            .build();

    messagingTemplate.convertAndSend(
            "/topic/room/" + roomId + "/users",
            message
    );

    // También enviar mensaje de sistema
    sendSystemMessageToRoom(roomId, user.getUsername() + " ha salido de la sala");
  }

  @Override
  public void notifyUserStatusChange(User user, String status) {
    log.info("WebSocket: User {} status changed to {}", user.getUsername(), status);

    UserStatusMessage statusMessage = UserStatusMessage.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .status(status)
            .timestamp(LocalDateTime.now())
            .build();

    messagingTemplate.convertAndSend("/topic/user-status", statusMessage);
  }

  @Override
  public void notifyMessageDeleted(Long roomId, Long messageId) {
    log.info("WebSocket: Notifying message {} deleted in room {}", messageId, roomId);

    MessageDeletedNotification notification = MessageDeletedNotification.builder()
            .messageId(messageId)
            .roomId(roomId)
            .timestamp(LocalDateTime.now())
            .build();

    messagingTemplate.convertAndSend(
            "/topic/room/" + roomId + "/deleted",
            notification
    );
  }

  @Override
  public void sendErrorToUser(String username, String errorMessage) {
    log.warn("WebSocket: Sending error to user {}: {}", username, errorMessage);

    ErrorMessage error = ErrorMessage.builder()
            .message(errorMessage)
            .timestamp(LocalDateTime.now())
            .build();

    messagingTemplate.convertAndSendToUser(
            username,
            "/queue/errors",
            error
    );
  }

  @Override
  public void sendSystemMessageToRoom(Long roomId, String message) {
    log.info("WebSocket: Sending system message to room {}: {}", roomId, message);

    SystemMessage systemMessage = SystemMessage.builder()
            .content(message)
            .type("SYSTEM")
            .timestamp(LocalDateTime.now())
            .build();

    messagingTemplate.convertAndSend(
            "/topic/room/" + roomId + "/system",
            systemMessage
    );
  }

  @Override
  public void notifyRoomUpdate(Long roomId, Object roomData) {
    log.info("WebSocket: Notifying room {} update", roomId);

    messagingTemplate.convertAndSend(
            "/topic/room/" + roomId + "/update",
            roomData
    );
  }
}

