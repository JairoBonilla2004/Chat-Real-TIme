package ec.edu.espe.chat_real_time.dto.mapperDTO;

import ec.edu.espe.chat_real_time.dto.response.UserAdminResponse;
import ec.edu.espe.chat_real_time.dto.response.UserGuestResponse;
import ec.edu.espe.chat_real_time.model.user.AdminProfile;
import ec.edu.espe.chat_real_time.model.user.GuestProfile;
import ec.edu.espe.chat_real_time.model.user.User;

public class UserMapper {

    public static UserGuestResponse toUserGuestResponse(GuestProfile guestProfile) {
        User user = guestProfile.getUser();
        return UserGuestResponse.builder()
                .id(user.getId())
                .role(user.getRoles().stream().findFirst().orElse(null).getName())
                .nickname(guestProfile.getNickname())
                .isGuest(user.isGuest())
                .guestExpiresAt(guestProfile.getExpiresAt())
                .isActive(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public static UserAdminResponse toUserAdminResponse(AdminProfile adminProfile) {
        User user = adminProfile.getUser();
        return UserAdminResponse.builder()
                .id(adminProfile.getUser().getId())
                .name(adminProfile.getFirstName())
                .lastName(adminProfile.getLastName())
                .email(adminProfile.getEmail())
                .username(user.getUsername())
                .phone(adminProfile.getPhone())
                .role(user.getRoles().stream().findFirst().orElse(null).getName())
                .enabled(user.getEnabled())
                .build();
    }



}
