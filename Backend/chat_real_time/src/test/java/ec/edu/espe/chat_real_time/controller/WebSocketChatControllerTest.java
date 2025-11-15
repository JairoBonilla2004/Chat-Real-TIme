package ec.edu.espe.chat_real_time.controller;

import ec.edu.espe.chat_real_time.Service.websocket.WebSocketService;
import ec.edu.espe.chat_real_time.dto.request.SendMessageRequest;
import ec.edu.espe.chat_real_time.dto.websocket.TypingIndicator;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.mockito.Mockito.*;

class WebSocketChatControllerTest {

    @Mock
    private WebSocketService webSocketService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private WebSocketChatController controller;

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");

        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsernameAndDeletedAtIsNull("testuser"))
                .thenReturn(Optional.of(mockUser));
    }

    @Test
    void testSendMessage() {
        Long roomId = 10L;
        SendMessageRequest request = new SendMessageRequest();
        request.setContent("Hola mundo");

        controller.sendMessage(roomId, request, authentication);

        verify(webSocketService, times(1))
                .sendMessageToRoom(request, mockUser);
    }

    @Test
    void testTypingIndicator() {
        Long roomId = 20L;
        TypingIndicator indicator = new TypingIndicator();
        indicator.setTyping(true);

        controller.handleTypingIndicator(roomId, indicator, authentication);

        verify(webSocketService, times(1))
                .sendTypingIndicator(roomId, indicator, mockUser);
    }

    @Test
    void testJoinRoom() {
        Long roomId = 5L;

        controller.handleJoinRoom(roomId, authentication);

        verify(webSocketService, times(1))
                .notifyUserJoinedRoom(roomId, mockUser);
    }

    @Test
    void testLeaveRoom() {
        Long roomId = 7L;

        controller.handleLeaveRoom(roomId, authentication);

        verify(webSocketService, times(1))
                .notifyUserLeftRoom(roomId, mockUser);
    }

    @Test
    void testUserNotFoundThrowsException() {
        when(userRepository.findByUsernameAndDeletedAtIsNull("testuser"))
                .thenReturn(Optional.empty());

        Long roomId = 99L;
        SendMessageRequest request = new SendMessageRequest();

        try {
            controller.sendMessage(roomId, request, authentication);
        } catch (RuntimeException ex) {
            assert(ex.getMessage().equals("Usuario no encontrado"));
        }
    }
}
