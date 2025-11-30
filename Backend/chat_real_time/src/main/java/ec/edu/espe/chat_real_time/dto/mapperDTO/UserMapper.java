package ec.edu.espe.chat_real_time.dto.mapperDTO;

import ec.edu.espe.chat_real_time.dto.response.UserAdminResponse;
import ec.edu.espe.chat_real_time.dto.response.UserGuestResponse;
import ec.edu.espe.chat_real_time.model.user.AdminProfile;
import ec.edu.espe.chat_real_time.model.user.GuestProfile;
import ec.edu.espe.chat_real_time.model.user.User;

public class UserMapper {

  public static UserGuestResponse toUserGuestResponse(GuestProfile guestProfile) {
    User user = guestProfile.getUser();
    String roleName = null;
    if (user.getRoles() != null) {
      roleName = user.getRoles().stream().findFirst().map(r -> r.getName()).orElse(null);
    }
    return UserGuestResponse.builder()
            .id(user.getId())
            .role(roleName)
            .nickname(guestProfile.getNickname())
            .isGuest(user.isGuest())
            .guestExpiresAt(guestProfile.getExpiresAt())
            .isActive(user.getEnabled())
            .createdAt(user.getCreatedAt())
            .build();
  }

  public static UserAdminResponse toUserAdminResponse(AdminProfile adminProfile) {
    User user = adminProfile.getUser();
    String roleName = null;
    if (user.getRoles() != null) {
      roleName = user.getRoles().stream().findFirst().map(r -> r.getName()).orElse(null);
    }
    return UserAdminResponse.builder()
            .id(adminProfile.getUser().getId())
            .name(adminProfile.getFirstName())
            .lastName(adminProfile.getLastName())
            .email(adminProfile.getEmail())
            .username(user.getUsername())
            .phone(adminProfile.getPhone())
            .role(roleName)
            .enabled(user.getEnabled())
            .build();
  }
}
