package ec.edu.espe.chat_real_time.controller;

import ec.edu.espe.chat_real_time.Service.room.RoomService;
import ec.edu.espe.chat_real_time.dto.request.CreateRoomRequest;
import ec.edu.espe.chat_real_time.dto.request.JoinRoomRequest;
import ec.edu.espe.chat_real_time.dto.response.ApiResponse;
import ec.edu.espe.chat_real_time.dto.response.RoomDetailResponse;
import ec.edu.espe.chat_real_time.dto.response.RoomResponse;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

  private final RoomService roomService;
  private final UserRepository userRepository;

  @PostMapping("/create")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<RoomResponse>> createRoom(
          @Valid @RequestBody CreateRoomRequest request,
          Authentication authentication
  ) {
    User user = getUserFromAuthentication(authentication);
    RoomResponse response = roomService.createRoom(request, user);
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Sala creada exitosamente", response));
  }

  @PostMapping("/join")
  public ResponseEntity<ApiResponse<RoomDetailResponse>> joinRoom(
          @Valid @RequestBody JoinRoomRequest request,
          Authentication authentication,
          HttpServletRequest httpRequest
  ) {
    User user = getUserFromAuthentication(authentication);
    RoomDetailResponse response = roomService.joinRoom(request, user, httpRequest);
    return ResponseEntity.ok(ApiResponse.success("Te has unido a la sala exitosamente", response));
  }

  @PostMapping("/{roomId}/leave")
  public ResponseEntity<ApiResponse<Void>> leaveRoom(
          @PathVariable Long roomId,
          Authentication authentication
  ) {
    User user = getUserFromAuthentication(authentication);
    roomService.leaveRoom(roomId, user);
    return ResponseEntity.ok(ApiResponse.success("Has salido de la sala exitosamente", null));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<RoomResponse>>> getAllActiveRooms() {
    List<RoomResponse> rooms = roomService.getAllActiveRooms();
    return ResponseEntity.ok(ApiResponse.success("Salas obtenidas exitosamente", rooms));
  }

  @GetMapping("/code/{roomCode}")
  public ResponseEntity<ApiResponse<RoomResponse>> getRoomByCode(@PathVariable String roomCode) {
    RoomResponse room = roomService.getRoomByCode(roomCode);
    return ResponseEntity.ok(ApiResponse.success("Sala encontrada", room));
  }

  @GetMapping("/{roomId}/details")
  public ResponseEntity<ApiResponse<RoomDetailResponse>> getRoomDetails(
          @PathVariable Long roomId
  ) {
    RoomDetailResponse details = roomService.getRoomDetails(roomId);
    return ResponseEntity.ok(ApiResponse.success("Detalles de sala obtenidos", details));
  }

  @GetMapping("/my-rooms")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<List<RoomResponse>>> getMyRooms(
          Authentication authentication
  ) {
    User user = getUserFromAuthentication(authentication);
    List<RoomResponse> rooms = roomService.getUserCreatedRooms(user);
    return ResponseEntity.ok(ApiResponse.success("Tus salas obtenidas exitosamente", rooms));
  }

  @PostMapping("/{roomId}/reset-pin")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<RoomResponse>> resetPin(
          @PathVariable Long roomId,
          Authentication authentication
  ) {
    User user = getUserFromAuthentication(authentication);
    RoomResponse response = roomService.resetRoomPin(roomId, user);
    return ResponseEntity.ok(ApiResponse.success("PIN reseteado correctamente", response));
  }

  private User getUserFromAuthentication(Authentication authentication) {
    String username = authentication.getName();
    return userRepository.findByUsernameAndDeletedAtIsNull(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
  }


}