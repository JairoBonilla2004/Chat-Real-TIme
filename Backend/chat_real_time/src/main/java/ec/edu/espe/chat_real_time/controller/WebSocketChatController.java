package ec.edu.espe.chat_real_time.controller;

import ec.edu.espe.chat_real_time.Service.websocket.WebSocketService;
import ec.edu.espe.chat_real_time.dto.request.SendMessageRequest;
import ec.edu.espe.chat_real_time.dto.websocket.TypingIndicator;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketChatController {

  private final WebSocketService webSocketService;
  private final UserRepository userRepository;

  @MessageMapping("/chat.sendMessage/{roomId}")
  public void sendMessage(
          @DestinationVariable Long roomId,
          @Payload SendMessageRequest request,
          Authentication authentication
  ) {
    User user = getUserFromAuthentication(authentication);
    request.setRoomId(roomId); // Asegurar que el roomId esté en el request
    webSocketService.sendMessageToRoom(request, user);
  }

  @MessageMapping("/chat.typing/{roomId}")
  public void handleTypingIndicator(
          @DestinationVariable Long roomId,
          @Payload TypingIndicator typingIndicator,
          Authentication authentication
  ) {
    User user = getUserFromAuthentication(authentication);
    webSocketService.sendTypingIndicator(roomId, typingIndicator, user);
  }

  @MessageMapping("/chat.joinRoom/{roomId}")
  public void handleJoinRoom(
          @DestinationVariable Long roomId,
          Authentication authentication
  ) {
    User user = getUserFromAuthentication(authentication);
    webSocketService.notifyUserJoinedRoom(roomId, user);
  }

  @MessageMapping("/chat.leaveRoom/{roomId}")
  public void handleLeaveRoom(
          @DestinationVariable Long roomId,
          Authentication authentication
  ) {
    User user = getUserFromAuthentication(authentication);
    webSocketService.notifyUserLeftRoom(roomId, user);
  }

  private User getUserFromAuthentication(Authentication authentication) {
    String username = authentication.getName();
    return userRepository.findByUsernameAndDeletedAtIsNull(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
  }
}