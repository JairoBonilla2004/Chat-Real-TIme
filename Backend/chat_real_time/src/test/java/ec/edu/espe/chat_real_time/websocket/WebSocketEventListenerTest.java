package ec.edu.espe.chat_real_time.websocket;

import ec.edu.espe.chat_real_time.Service.websocket.WebSocketService;
import ec.edu.espe.chat_real_time.model.room.Room;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.model.user.UserSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WebSocketEventListenerTest {

    @Mock
    private WebSocketService webSocketService;

    @Mock
    private ec.edu.espe.chat_real_time.repository.UserRepository userRepository;

    @Mock
    private RoomSubscriptionTracker subscriptionTracker;

    @Mock
    private ec.edu.espe.chat_real_time.repository.UserSessionRepository userSessionRepository;

    @Mock
    private ec.edu.espe.chat_real_time.repository.RoomRepository roomRepository;

    @InjectMocks
    private WebSocketEventListener listener;

    @Test
    void handleWebSocketConnectListener_callsNotifyOnline() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(org.springframework.messaging.simp.stomp.StompCommand.CONNECT);
        Principal p = () -> "alice";
        accessor.setUser(p);
        Message<byte[]> msg = org.springframework.messaging.support.MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        User user = new User(); user.setUsername("alice");
        when(userRepository.findByUsernameAndDeletedAtIsNull("alice")).thenReturn(java.util.Optional.of(user));

        listener.handleWebSocketConnectListener(new SessionConnectedEvent(this, msg));

        verify(webSocketService).notifyUserStatusChange(user, "ONLINE");
    }

    @Test
    void handleWebSocketConnectListener_noUser_doesNothing() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(org.springframework.messaging.simp.stomp.StompCommand.CONNECT);
        // no principal
        Message<byte[]> msg = org.springframework.messaging.support.MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        listener.handleWebSocketConnectListener(new SessionConnectedEvent(this, msg));

        verifyNoInteractions(webSocketService);
    }

    @Test
    void handleWebSocketDisconnectListener_noPrincipal_doesNothing() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(org.springframework.messaging.simp.stomp.StompCommand.DISCONNECT);
        accessor.setSessionId("s1");
        Message<byte[]> msg = org.springframework.messaging.support.MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        SessionDisconnectEvent ev = mock(SessionDisconnectEvent.class);
        when(ev.getMessage()).thenReturn(msg);
        listener.handleWebSocketDisconnectListener(ev);

        verifyNoInteractions(userRepository, subscriptionTracker, userSessionRepository, roomRepository, webSocketService);
    }

    @Test
    void handleWebSocketDisconnectListener_userNotFound_noActions() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(org.springframework.messaging.simp.stomp.StompCommand.DISCONNECT);
        Principal p = () -> "bob";
        accessor.setUser(p);
        accessor.setSessionId("sess-bob");
        Message<byte[]> msg = org.springframework.messaging.support.MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(userRepository.findByUsernameAndDeletedAtIsNull("bob")).thenReturn(Optional.empty());

        SessionDisconnectEvent ev2 = mock(SessionDisconnectEvent.class);
        when(ev2.getMessage()).thenReturn(msg);
        listener.handleWebSocketDisconnectListener(ev2);

        verify(userRepository).findByUsernameAndDeletedAtIsNull("bob");
        verifyNoMoreInteractions(subscriptionTracker, userSessionRepository, roomRepository, webSocketService);
    }

    @Test
    void handleWebSocketDisconnectListener_noSubscription_callsOffline() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(org.springframework.messaging.simp.stomp.StompCommand.DISCONNECT);
        Principal p = () -> "carol";
        accessor.setUser(p);
        accessor.setSessionId("sess-carol");
        Message<byte[]> msg = org.springframework.messaging.support.MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        User user = new User(); user.setUsername("carol");
        when(userRepository.findByUsernameAndDeletedAtIsNull("carol")).thenReturn(Optional.of(user));
        when(subscriptionTracker.getRoomId("sess-carol")).thenReturn(Optional.empty());

        SessionDisconnectEvent ev3 = mock(SessionDisconnectEvent.class);
        when(ev3.getMessage()).thenReturn(msg);
        listener.handleWebSocketDisconnectListener(ev3);

        verify(subscriptionTracker).getRoomId("sess-carol");
        verify(subscriptionTracker, never()).unmap(anyString());
        verify(webSocketService).notifyUserStatusChange(user, "OFFLINE");
        verifyNoMoreInteractions(roomRepository, userSessionRepository);
    }

    @Test
    void handleWebSocketDisconnectListener_roomNotFound_unmapsAndOffline() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(org.springframework.messaging.simp.stomp.StompCommand.DISCONNECT);
        Principal p = () -> "dave";
        accessor.setUser(p);
        accessor.setSessionId("sess-dave");
        Message<byte[]> msg = org.springframework.messaging.support.MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        User user = new User(); user.setUsername("dave");
        when(userRepository.findByUsernameAndDeletedAtIsNull("dave")).thenReturn(Optional.of(user));
        when(subscriptionTracker.getRoomId("sess-dave")).thenReturn(Optional.of(11L));
        when(roomRepository.findByIdAndDeletedAtIsNull(11L)).thenReturn(Optional.empty());

        SessionDisconnectEvent ev4 = mock(SessionDisconnectEvent.class);
        when(ev4.getMessage()).thenReturn(msg);
        listener.handleWebSocketDisconnectListener(ev4);

        verify(subscriptionTracker).getRoomId("sess-dave");
        verify(roomRepository).findByIdAndDeletedAtIsNull(11L);
        verify(subscriptionTracker).unmap("sess-dave");
        verify(webSocketService).notifyUserStatusChange(user, "OFFLINE");
        verifyNoInteractions(userSessionRepository);
    }

    @Test
    void handleWebSocketDisconnectListener_activeSessionNull_unmapsAndOffline() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(org.springframework.messaging.simp.stomp.StompCommand.DISCONNECT);
        Principal p = () -> "ellen";
        accessor.setUser(p);
        accessor.setSessionId("sess-ellen");
        Message<byte[]> msg = org.springframework.messaging.support.MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        User user = new User(); user.setUsername("ellen");
        Room room = Room.builder().id(20L).currentUsers(2).build();

        when(userRepository.findByUsernameAndDeletedAtIsNull("ellen")).thenReturn(Optional.of(user));
        when(subscriptionTracker.getRoomId("sess-ellen")).thenReturn(Optional.of(20L));
        when(roomRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(room));
        when(userSessionRepository.findByUserAndRoomAndIsActiveTrue(user, room)).thenReturn(Optional.empty());

        SessionDisconnectEvent ev5 = mock(SessionDisconnectEvent.class);
        when(ev5.getMessage()).thenReturn(msg);
        listener.handleWebSocketDisconnectListener(ev5);

        verify(userSessionRepository).findByUserAndRoomAndIsActiveTrue(user, room);
        verify(subscriptionTracker).unmap("sess-ellen");
        verify(webSocketService).notifyUserStatusChange(user, "OFFLINE");
        verify(webSocketService, never()).notifyUserLeftRoom(anyLong(), any());
        verify(roomRepository, never()).save(any());
    }

    @Test
    void handleWebSocketDisconnectListener_activeSessionPresent_handlesCleanup() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(org.springframework.messaging.simp.stomp.StompCommand.DISCONNECT);
        Principal p = () -> "frank";
        accessor.setUser(p);
        accessor.setSessionId("sess-frank");
        Message<byte[]> msg = org.springframework.messaging.support.MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        User user = new User(); user.setUsername("frank");
        Room room = Room.builder().id(30L).currentUsers(3).build();
        UserSession session = UserSession.builder().isActive(true).user(user).room(room).build();

        when(userRepository.findByUsernameAndDeletedAtIsNull("frank")).thenReturn(Optional.of(user));
        when(subscriptionTracker.getRoomId("sess-frank")).thenReturn(Optional.of(30L));
        when(roomRepository.findByIdAndDeletedAtIsNull(30L)).thenReturn(Optional.of(room));
        when(userSessionRepository.findByUserAndRoomAndIsActiveTrue(user, room)).thenReturn(Optional.of(session));

        SessionDisconnectEvent ev6 = mock(SessionDisconnectEvent.class);
        when(ev6.getMessage()).thenReturn(msg);
        listener.handleWebSocketDisconnectListener(ev6);

        // verify session saved with isActive false and leftAt set
        ArgumentCaptor<UserSession> capt = ArgumentCaptor.forClass(UserSession.class);
        verify(userSessionRepository).save(capt.capture());
        UserSession saved = capt.getValue();
        assertFalse(saved.getIsActive());
        assertNotNull(saved.getLeftAt());

        // room decremented and saved
        assertEquals(2, room.getCurrentUsers());
        verify(roomRepository).save(room);

        verify(webSocketService).notifyUserLeftRoom(room.getId(), user);
        verify(subscriptionTracker).unmap("sess-frank");
        verify(webSocketService).notifyUserStatusChange(user, "OFFLINE");
    }
}
