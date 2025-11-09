package ec.edu.espe.chat_real_time.controller;


import ec.edu.espe.chat_real_time.Service.message.MessageService;
import ec.edu.espe.chat_real_time.dto.request.SendMessageRequest;
import ec.edu.espe.chat_real_time.dto.response.ApiResponse;
import ec.edu.espe.chat_real_time.dto.response.MessageResponse;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

  private final MessageService messageService;
  private final UserRepository userRepository;

  @PostMapping("/text")
  public ResponseEntity<ApiResponse<MessageResponse>> sendTextMessage(
          @Valid @RequestBody SendMessageRequest request,
          Authentication authentication
  ) {
    User user = getUserFromAuthentication(authentication);
    MessageResponse response = messageService.sendTextMessage(request, user);
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Mensaje enviado exitosamente", response));
  }

  @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<MessageResponse>> sendFileMessage(
          @RequestParam Long roomId,
          @RequestParam(required = false) String content,
          @RequestParam("file") MultipartFile file,
          Authentication authentication
  ) {
    User user = getUserFromAuthentication(authentication);
    MessageResponse response = messageService.sendFileMessage(roomId, content, file, user);
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Archivo enviado exitosamente", response));
  }

  @GetMapping("/room/{roomId}")
  public ResponseEntity<ApiResponse<List<MessageResponse>>> getRoomMessages(
          @PathVariable Long roomId,
          Authentication authentication
  ) {
    User user = getUserFromAuthentication(authentication);
    List<MessageResponse> messages = messageService.getRoomMessages(roomId, user);
    return ResponseEntity.ok(ApiResponse.success("Mensajes obtenidos exitosamente", messages));
  }

  @GetMapping("/{messageId}")
  public ResponseEntity<ApiResponse<MessageResponse>> getMessageById(
          @PathVariable Long messageId
  ) {
    MessageResponse message = messageService.getMessageById(messageId);
    return ResponseEntity.ok(ApiResponse.success("Mensaje encontrado", message));
  }

  @DeleteMapping("/{messageId}")
  public ResponseEntity<ApiResponse<Void>> deleteMessage(
          @PathVariable Long messageId,
          Authentication authentication
  ) {
    User user = getUserFromAuthentication(authentication);
    messageService.deleteMessage(messageId, user);
    return ResponseEntity.ok(ApiResponse.success("Mensaje eliminado exitosamente", null));
  }

  private User getUserFromAuthentication(Authentication authentication) {
    String username = authentication.getName();
    return userRepository.findByUsernameAndDeletedAtIsNull(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
  }
}