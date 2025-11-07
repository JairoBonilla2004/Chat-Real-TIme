package ec.edu.espe.chat_real_time.dto.mapperDTO;

import ec.edu.espe.chat_real_time.dto.response.UserAdminResponse;
import ec.edu.espe.chat_real_time.dto.response.UserGuestResponse;
import ec.edu.espe.chat_real_time.model.user.User;

public class UserMapper {

  public static UserGuestResponse toUserGuestResponse(User user) {
    return UserGuestResponse.builder()
            .id(user.getId())
            .role(user.getRole())
            .nickname(user.getNickname())
            .isGuest(user.getIsGuest())
            .guestExpiresAt(user.getGuestExpiresAt())
            .isActive(user.getIsActive())
            .createdAt(user.getCreatedAt())
            .build();
  }

  public static UserAdminResponse toUserAdminResponse(User user) {
    return UserAdminResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .username(user.getUsername())
            .phone(user.getPhone())
            .role(user.getRole().name())
            .enabled(user.getIsActive())
            .build();
  }
}
