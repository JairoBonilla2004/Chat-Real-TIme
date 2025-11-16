package ec.edu.espe.chat_real_time.Service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import ec.edu.espe.chat_real_time.Service.message.MessageService;
import ec.edu.espe.chat_real_time.Service.websocket.WebSocketServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {WebSocketServiceImpl.class})
@DisabledInAotMode
@ExtendWith(SpringExtension.class)
class WebSocketServiceImplTest {
    @MockitoBean
    private MessageService messageService;

    @MockitoBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private WebSocketServiceImpl webSocketServiceImpl;

    /**
     * Test {@link WebSocketServiceImpl#notifyMessageDeleted(Long, Long)}.
     *
     * <p>Method under test: {@link WebSocketServiceImpl#notifyMessageDeleted(Long, Long)}
     */
    @Test
    @DisplayName("Test notifyMessageDeleted(Long, Long)")
    @Tag("MaintainedByDiffblue")
    void testNotifyMessageDeleted() throws MessagingException {
        // Arrange
        doNothing()
                .when(simpMessagingTemplate)
                .convertAndSend(Mockito.<String>any(), Mockito.<Object>any());

        // Act
        webSocketServiceImpl.notifyMessageDeleted(1L, 1L);

        // Assert
        verify(simpMessagingTemplate).convertAndSend(eq("/topic/room/1/deleted"), isA(Object.class));
    }

    /**
     * Test {@link WebSocketServiceImpl#notifyRoomUpdate(Long, Object)}.
     *
     * <p>Method under test: {@link WebSocketServiceImpl#notifyRoomUpdate(Long, Object)}
     */
    @Test
    @DisplayName("Test notifyRoomUpdate(Long, Object)")
    @Tag("MaintainedByDiffblue")
    void testNotifyRoomUpdate() throws MessagingException {
        // Arrange
        doNothing()
                .when(simpMessagingTemplate)
                .convertAndSend(Mockito.<String>any(), Mockito.<Object>any());

        // Act
        webSocketServiceImpl.notifyRoomUpdate(1L, "Room Data");

        // Assert
        verify(simpMessagingTemplate).convertAndSend(eq("/topic/room/1/update"), isA(Object.class));
    }
}

