package ec.edu.espe.chat_real_time.dto.mapperDTO;

import ec.edu.espe.chat_real_time.dto.response.UserAdminResponse;
import ec.edu.espe.chat_real_time.dto.response.UserGuestResponse;
import ec.edu.espe.chat_real_time.model.Role;
import ec.edu.espe.chat_real_time.model.user.AdminProfile;
import ec.edu.espe.chat_real_time.model.user.GuestProfile;
import ec.edu.espe.chat_real_time.model.user.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toUserGuestResponse_mapsCorrectly() {
        Role role = Role.builder().id(1L).name("ROLE_GUEST").build();
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .id(5L)
                .username("guest1")
                .enabled(true)
                .roles(roles)
                .createdAt(LocalDateTime.now())
                .build();

        GuestProfile guest = GuestProfile.builder()
                .id(5L)
                .nickname("nickgy")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .user(user)
                .build();
        user.setGuestProfile(guest);

        UserGuestResponse res = UserMapper.toUserGuestResponse(guest);

        assertNotNull(res);
        assertEquals(user.getId(), res.getId());
        assertEquals("ROLE_GUEST", res.getRole());
        assertEquals("nickgy", res.getNickname());
        assertTrue(res.getIsGuest());
        assertNotNull(res.getGuestExpiresAt());
        assertEquals(user.getEnabled(), res.getIsActive());
        assertNotNull(res.getCreatedAt());
    }

    @Test
    void toUserAdminResponse_mapsCorrectly() {
        Role role = Role.builder().id(2L).name("ROLE_ADMIN").build();
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .id(6L)
                .username("admin1")
                .enabled(false)
                .roles(roles)
                .build();

        AdminProfile admin = AdminProfile.builder()
                .id(6L)
                .firstName("Admin")
                .lastName("One")
                .email("admin@example.com")
                .phone("999")
                .user(user)
                .build();
        user.setAdminProfile(admin);

        UserAdminResponse res = UserMapper.toUserAdminResponse(admin);

        assertNotNull(res);
        assertEquals(admin.getUser().getId(), res.getId());
        assertEquals(admin.getFirstName(), res.getName());
        assertEquals(admin.getLastName(), res.getLastName());
        assertEquals(admin.getEmail(), res.getEmail());
        assertEquals(user.getUsername(), res.getUsername());
        assertEquals(admin.getPhone(), res.getPhone());
        assertEquals("ROLE_ADMIN", res.getRole());
        assertEquals(user.getEnabled(), res.isEnabled());
    }
}
