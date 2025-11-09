package ec.edu.espe.chat_real_time.Service.room;

import ec.edu.espe.chat_real_time.dto.request.CreateRoomRequest;
import ec.edu.espe.chat_real_time.dto.request.JoinRoomRequest;
import ec.edu.espe.chat_real_time.dto.response.RoomDetailResponse;
import ec.edu.espe.chat_real_time.dto.response.RoomResponse;
import ec.edu.espe.chat_real_time.model.user.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface RoomService {
  RoomResponse createRoom(CreateRoomRequest request, User creator);
  RoomDetailResponse joinRoom(JoinRoomRequest request, User user, HttpServletRequest httpRequest);
  void leaveRoom(Long roomId, User user);
  RoomResponse getRoomByCode(String roomCode);
  RoomDetailResponse getRoomDetails(Long roomId);
  List<RoomResponse> getAllActiveRooms();
  List<RoomResponse> getUserCreatedRooms(User user);
  boolean validateRoomPin(String roomCode, String pin);
}