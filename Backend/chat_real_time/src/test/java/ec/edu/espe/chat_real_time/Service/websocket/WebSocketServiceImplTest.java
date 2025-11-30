package ec.edu.espe.chat_real_time.Service.websocket;

import ec.edu.espe.chat_real_time.Service.message.MessageService;
import ec.edu.espe.chat_real_time.dto.request.SendMessageRequest;
import ec.edu.espe.chat_real_time.dto.response.MessageResponse;
import ec.edu.espe.chat_real_time.dto.websocket.ErrorMessage;
import ec.edu.espe.chat_real_time.dto.websocket.MessageDeletedNotification;
import ec.edu.espe.chat_real_time.dto.websocket.TypingIndicator;
import ec.edu.espe.chat_real_time.dto.websocket.UserJoinedMessage;
import ec.edu.espe.chat_real_time.model.Role;
import ec.edu.espe.chat_real_time.model.room.Room;
import ec.edu.espe.chat_real_time.model.user.AdminProfile;
import ec.edu.espe.chat_real_time.model.user.GuestProfile;
import ec.edu.espe.chat_real_time.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketServiceImplTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private WebSocketServiceImpl service;

    @Captor
    ArgumentCaptor<UserJoinedMessage> userJoinedCaptor;

    @Captor
    ArgumentCaptor<MessageDeletedNotification> messageDeletedCaptor;

    @Captor
    ArgumentCaptor<Object> genericCaptor;

    @BeforeEach
    void setUp() {
    }

    @Test
    void sendMessageToRoom_success_sendsViaMessagingTemplate_and_returnsMessage() {
        SendMessageRequest req = new SendMessageRequest();
        req.setRoomId(10L);
        req.setContent("hola");

        User user = User.builder().id(1L).username("u1").build();

        MessageResponse msg = MessageResponse.builder().id(99L).content("hola").sentAt(LocalDateTime.now()).build();

        when(messageService.sendTextMessage(req, user)).thenReturn(msg);

        MessageResponse res = service.sendMessageToRoom(req, user);

        assertEquals(msg, res);
        verify(messagingTemplate).convertAndSend(eq("/topic/room/10"), eq(msg));
        verify(messageService).sendTextMessage(req, user);
    }

    @Test
    void sendMessageToRoom_exception_callsSendErrorToUser_and_throws() {
        SendMessageRequest req = new SendMessageRequest();
        req.setRoomId(11L);
        User user = User.builder().id(2L).username("bob").build();

        when(messageService.sendTextMessage(req, user)).thenThrow(new RuntimeException("boom"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.sendMessageToRoom(req, user));
        assertEquals("boom", ex.getMessage());

        verify(messagingTemplate).convertAndSendToUser(eq(user.getUsername()), eq("/queue/errors"), any(ErrorMessage.class));
    }

    @Test
    void sendTypingIndicator_setsUserFields_and_sends() {
        TypingIndicator indicator = TypingIndicator.builder().isTyping(true).build();
        User user = User.builder().id(3L).username("chico").build();

        service.sendTypingIndicator(20L, indicator, user);

        assertEquals(user.getId(), indicator.getUserId());
        assertEquals(user.getUsername(), indicator.getUsername());
        assertEquals(20L, indicator.getRoomId());

        verify(messagingTemplate).convertAndSend(eq("/topic/room/20/typing"), eq(indicator));
    }

    @Test
    void notifyUserJoinedRoom_adminUsesAdminDisplayName_and_sendsSystemMessage() {
        Role role = Role.builder().id(1L).name("ROLE_ADMIN").build();
        Set<Role> roles = new HashSet<>(); roles.add(role);
        User user = User.builder().id(4L).username("admin").roles(roles).build();
        AdminProfile admin = AdminProfile.builder().id(4L).firstName("John").lastName("Doe").user(user).build();
        user.setAdminProfile(admin);

        service.notifyUserJoinedRoom(7L, user);

        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/room/7/users"), userJoinedCaptor.capture());
        UserJoinedMessage uj = userJoinedCaptor.getValue();
        assertTrue(uj.getUsername().contains("John"));
        assertTrue(uj.getUsername().contains("(Admin)"));

        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/room/7/system"), genericCaptor.capture());
    }

    @Test
    void notifyUserJoinedRoom_guestUsesNickname() {
        Role role = Role.builder().id(2L).name("ROLE_GUEST").build();
        Set<Role> roles = new HashSet<>(); roles.add(role);
        User user = User.builder().id(5L).username("guser").roles(roles).build();
        GuestProfile guest = GuestProfile.builder().id(5L).nickname("gnick").user(user).build();
        user.setGuestProfile(guest);

        service.notifyUserJoinedRoom(8L, user);

        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/room/8/users"), userJoinedCaptor.capture());
        UserJoinedMessage uj2 = userJoinedCaptor.getValue();
        assertEquals("gnick", uj2.getUsername());
    }

    @Test
    void notifyMessageDeleted_sendsNotification() {
        service.notifyMessageDeleted(12L, 77L);
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/room/12/deleted"), messageDeletedCaptor.capture());
        MessageDeletedNotification n = messageDeletedCaptor.getValue();
        assertEquals(77L, n.getMessageId());
        assertEquals(12L, n.getRoomId());
    }

    @Test
    void notifyRoomUpdate_sendsRoomData() {
        Object roomObj = new Room();
        service.notifyRoomUpdate(3L, roomObj);
        verify(messagingTemplate).convertAndSend(eq("/topic/room/3/update"), eq(roomObj));
    }
}
