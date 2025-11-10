package ec.edu.espe.chat_real_time.Service.websocket;


import ec.edu.espe.chat_real_time.dto.request.SendMessageRequest;
import ec.edu.espe.chat_real_time.dto.response.MessageResponse;
import ec.edu.espe.chat_real_time.dto.websocket.TypingIndicator;
import ec.edu.espe.chat_real_time.model.user.User;

public interface WebSocketService {

  MessageResponse sendMessageToRoom(SendMessageRequest request, User user);

  void sendTypingIndicator(Long roomId, TypingIndicator typingIndicator, User user);

  void notifyUserJoinedRoom(Long roomId, User user);

  void notifyUserLeftRoom(Long roomId, User user);

  void notifyUserStatusChange(User user, String status);

  void notifyMessageDeleted(Long roomId, Long messageId);

  void sendErrorToUser(String username, String errorMessage);

  void sendSystemMessageToRoom(Long roomId, String message);

  void notifyRoomUpdate(Long roomId, Object roomData);
}