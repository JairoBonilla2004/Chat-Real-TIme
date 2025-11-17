package ec.edu.espe.chat_real_time.dto.mapperDTO;

import ec.edu.espe.chat_real_time.dto.response.UserAdminResponse;
import ec.edu.espe.chat_real_time.dto.response.RoomResponse;
import ec.edu.espe.chat_real_time.model.Role;
import ec.edu.espe.chat_real_time.model.room.Room;
import ec.edu.espe.chat_real_time.model.room.RoomType;
import ec.edu.espe.chat_real_time.model.user.AdminProfile;
import ec.edu.espe.chat_real_time.model.user.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RoomMapperTest {

    @Test
    void toRoomResponse_mapsAllFields_and_creatorMapped() {
        Role role = Role.builder().id(1L).name("ADMIN").build();
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .id(10L)
                .username("jdoe")
                .enabled(true)
                .roles(roles)
                .build();

        AdminProfile admin = AdminProfile.builder()
                .id(10L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .user(user)
                .build();
        user.setAdminProfile(admin);

        LocalDateTime now = LocalDateTime.now();

        Room room = Room.builder()
                .id(100L)
                .roomCode("ROOM100")
                .name("Sala de prueba")
                .description("Descripción")
                .type(RoomType.TEXT)
                .maxUsers(5)
                .currentUsers(5)
                .maxFileSizeMb(15)
                .isActive(true)
                .createdAt(now)
                .creator(user)
                .build();

        RoomResponse res = RoomMapper.toRoomResponse(room);

        assertNotNull(res);
        assertEquals(room.getId(), res.getId());
        assertEquals(room.getRoomCode(), res.getRoomCode());
        assertEquals(room.getName(), res.getName());
        assertEquals(room.getDescription(), res.getDescription());
        assertEquals(room.getType(), res.getType());
        assertEquals(room.getMaxUsers(), res.getMaxUsers());
        assertEquals(room.getCurrentUsers(), res.getCurrentUsers());
        assertEquals(room.getMaxFileSizeMb(), res.getMaxFileSizeMb());
        assertEquals(room.getIsActive(), res.getIsActive());
        assertEquals(true, res.getIsFull()); // currentUsers == maxUsers -> full
        assertEquals(now, res.getCreatedAt());

        UserAdminResponse creator = res.getCreator();
        assertNotNull(creator);
        assertEquals(admin.getFirstName(), creator.getName());
        assertEquals(admin.getLastName(), creator.getLastName());
        assertEquals(admin.getEmail(), creator.getEmail());
        assertEquals(user.getUsername(), creator.getUsername());
        assertEquals(admin.getPhone(), creator.getPhone());
        assertEquals("ADMIN", creator.getRole());
        assertTrue(creator.isEnabled());
    }

    @Test
    void toRoomResponse_isFull_falseWhenBelowMax() {
        Role role = Role.builder().id(2L).name("USER").build();
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .id(11L)
                .username("alice")
                .enabled(false)
                .roles(roles)
                .build();

        AdminProfile admin = AdminProfile.builder()
                .id(11L)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .phone("000")
                .user(user)
                .build();
        user.setAdminProfile(admin);

        Room room = Room.builder()
                .id(101L)
                .roomCode("ROOM101")
                .name("Otra sala")
                .maxUsers(10)
                .currentUsers(3)
                .creator(user)
                .build();

        RoomResponse res = RoomMapper.toRoomResponse(room);
        assertNotNull(res);
        assertFalse(res.getIsFull());
        assertEquals("Alice", res.getCreator().getName());
        assertEquals("USER", res.getCreator().getRole());
        assertFalse(res.getCreator().isEnabled());
    }
}

