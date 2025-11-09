package ec.edu.espe.chat_real_time.dto.mapperDTO;

import ec.edu.espe.chat_real_time.dto.response.RoomResponse;
import ec.edu.espe.chat_real_time.model.room.Room;
import ec.edu.espe.chat_real_time.model.user.User;

public class RoomMapper {

  public static RoomResponse toRoomResponse(Room room) {
    User creator = room.getCreator();
    return RoomResponse.builder()
            .id(room.getId())
            .roomCode(room.getRoomCode())
            .name(room.getName())
            .description(room.getDescription())
            .type(room.getType())
            .maxUsers(room.getMaxUsers())
            .currentUsers(room.getCurrentUsers())
            .maxFileSizeMb(room.getMaxFileSizeMb())
            .isActive(room.getIsActive())
            .isFull(room.isFull())
            .createdAt(room.getCreatedAt())
            .creator(UserMapper.toUserAdminResponse(creator.getAdminProfile())) // el creador de l
            .build();
  }
}
