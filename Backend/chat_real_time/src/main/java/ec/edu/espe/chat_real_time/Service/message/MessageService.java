package ec.edu.espe.chat_real_time.Service.message;

import ec.edu.espe.chat_real_time.dto.request.SendMessageRequest;
import ec.edu.espe.chat_real_time.dto.response.MessageResponse;
import ec.edu.espe.chat_real_time.model.user.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MessageService {
  MessageResponse sendTextMessage(SendMessageRequest request, User user);
  MessageResponse sendFileMessage(Long roomId, String content, MultipartFile file, User user);
  List<MessageResponse> getRoomMessages(Long roomId, User user);
  MessageResponse getMessageById(Long messageId);
  void deleteMessage(Long messageId, User user);
}
