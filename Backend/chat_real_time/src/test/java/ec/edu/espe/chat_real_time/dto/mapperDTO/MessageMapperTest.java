package ec.edu.espe.chat_real_time.dto.mapperDTO;

import ec.edu.espe.chat_real_time.dto.response.AttachmentResponse;
import ec.edu.espe.chat_real_time.dto.response.MessageResponse;
import ec.edu.espe.chat_real_time.model.Attachment;
import ec.edu.espe.chat_real_time.model.Role;
import ec.edu.espe.chat_real_time.model.message.Message;
import ec.edu.espe.chat_real_time.model.message.MessageType;
import ec.edu.espe.chat_real_time.model.room.Room;
import ec.edu.espe.chat_real_time.model.user.AdminProfile;
import ec.edu.espe.chat_real_time.model.user.GuestProfile;
import ec.edu.espe.chat_real_time.model.user.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MessageMapperTest {

    @Test
    void toMessageResponse_mapsAdminSender_and_includesAttachments_whenNotDeleted() {
        Role role = Role.builder().id(1L).name("ROLE_ADMIN").build();
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder().id(20L).username("admin2").roles(roles).enabled(true).build();
        AdminProfile admin = AdminProfile.builder().id(20L).firstName("A").lastName("B").email("a@b.com").user(user).build();
        user.setAdminProfile(admin);

        Room room = Room.builder().id(300L).build();

        Attachment att = new Attachment();
        att.setId(77L);
        att.setFileName("f.txt");
        att.setOriginalFileName("original-f.txt");
        att.setFileType("text/plain");
        att.setFileSize(123L);
        att.setFileUrl("http://example/file");
        att.setUploadedAt(LocalDateTime.now());

        Message message = Message.builder()
                .id(500L)
                .content("hello")
                .messageType(MessageType.TEXT)
                .sentAt(LocalDateTime.now())
                .isEdited(false)
                .isDeleted(false)
                .user(user)
                .room(room)
                .build();
        message.getAttachments().add(att);

        MessageResponse res = MessageMapper.toMessageResponse(message);

        assertNotNull(res);
        assertEquals(message.getId(), res.getId());
        assertEquals(message.getContent(), res.getContent());
        assertEquals(message.getMessageType(), res.getMessageType());
        assertEquals(message.getSentAt(), res.getSentAt());
        assertFalse(res.isDeleted());
        assertTrue(res.getSenderNickname().contains("Admin"));
        assertEquals(user.getId(), res.getSenderId());
        assertEquals(room.getId(), res.getRoomId());

        List<AttachmentResponse> attachments = res.getAttachments();
        assertNotNull(attachments);
        assertEquals(1, attachments.size());
        AttachmentResponse ar = attachments.get(0);
        assertEquals(att.getId(), ar.getId());
        assertEquals(att.getFileName(), ar.getFileName());
    }

    @Test
    void toMessageResponse_guestSender_and_deletedMessage_hidesContentAndAttachments() {
        Role role = Role.builder().id(2L).name("SOME").build();
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder().id(21L).username("guestX").roles(roles).enabled(true).build();
        GuestProfile guest = GuestProfile.builder().id(21L).nickname("gNick").user(user).build();
        user.setGuestProfile(guest);

        Room room = Room.builder().id(301L).build();

        Message message = Message.builder()
                .id(501L)
                .content("secret")
                .messageType(MessageType.TEXT)
                .sentAt(LocalDateTime.now())
                .isEdited(false)
                .isDeleted(true)
                .user(user)
                .room(room)
                .build();

        MessageResponse res = MessageMapper.toMessageResponse(message);

        assertNotNull(res);
        assertEquals("", res.getContent());
        assertTrue(res.isDeleted());
        assertEquals("gNick", res.getSenderNickname());
        assertEquals(0, res.getAttachments().size());
    }
}
