package ec.edu.espe.chat_real_time.Service;
import ec.edu.espe.chat_real_time.Service.device.DeviceSessionService;
import ec.edu.espe.chat_real_time.Service.room.RoomServiceImpl;
import ec.edu.espe.chat_real_time.Service.websocket.WebSocketService;
import ec.edu.espe.chat_real_time.dto.request.JoinRoomRequest;
import ec.edu.espe.chat_real_time.exception.BadRequestException;
import ec.edu.espe.chat_real_time.exception.ResourceNotFoundException;
import ec.edu.espe.chat_real_time.exception.RoomFullException;
import ec.edu.espe.chat_real_time.model.room.Room;
import ec.edu.espe.chat_real_time.model.user.User;
import java.util.Optional;
import ec.edu.espe.chat_real_time.repository.MessageRepository;
import ec.edu.espe.chat_real_time.repository.RoomRepository;
import ec.edu.espe.chat_real_time.repository.UserRepository;
import ec.edu.espe.chat_real_time.repository.UserSessionRepository;
import ec.edu.espe.chat_real_time.security.jwt.JwtService;
import ec.edu.espe.chat_real_time.utils.PinGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.*;

import static org.mockito.Mockito.when;

class RoomServiceImplTest {

    private RoomRepository roomRepository;
    private UserSessionRepository sessionRepository;
    private MessageRepository messageRepository;
    private PasswordEncoder passwordEncoder;
    private PinGenerator pinGeneratorService;
    private DeviceSessionService deviceSessionService;
    private HttpRequestService httpRequestService;
    private WebSocketService webSocketService;

    private RoomServiceImpl roomService;
    private User user;

    @BeforeEach
    void setUp() {
        roomRepository = mock(RoomRepository.class);
        sessionRepository = mock(UserSessionRepository.class);
        messageRepository = mock(MessageRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        pinGeneratorService = mock(PinGenerator.class);
        deviceSessionService = mock(DeviceSessionService.class);
        httpRequestService = mock(HttpRequestService.class);
        webSocketService = mock(WebSocketService.class);

        user = mock(User.class);

        roomService = new RoomServiceImpl(
                roomRepository,
                sessionRepository,
                messageRepository,
                passwordEncoder,
                pinGeneratorService,
                deviceSessionService,
                httpRequestService,
                webSocketService
        );
    }
    @Test
    void createRoom_ShouldReturnRoomResponse() {

    }

    @Test
    void joinRoom_ShouldThrowIfRoomNotFound() {
        JoinRoomRequest request = new JoinRoomRequest();
        request.setRoomCode("ROOM999");

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);

        when(roomRepository.findByRoomCodeAndDeletedAtIsNull("ROOM999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.joinRoom(request, user, httpRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Sala no encontrada");
    }

    @Test
    void joinRoom_ShouldThrowIfPinIncorrect() {
        JoinRoomRequest request = new JoinRoomRequest();
        request.setRoomCode("ROOM123");
        request.setPin("9999");

        Room room = Room.builder()
                .roomCode("ROOM123")
                .isActive(true)
                .build();

        when(roomRepository.findByRoomCodeAndDeletedAtIsNull("ROOM123")).thenReturn(Optional.of(room));
        when(pinGeneratorService.validatePin("9999", room.getPinHash())).thenReturn(false);

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);

        assertThatThrownBy(() -> roomService.joinRoom(request, user, httpRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("PIN incorrecto");
    }

    @Test
    void joinRoom_ShouldThrowIfRoomFull() {
        JoinRoomRequest request = new JoinRoomRequest();
        request.setRoomCode("ROOM123");
        request.setPin("1234");

        Room room = Room.builder()
                .roomCode("ROOM123")
                .isActive(true)
                .currentUsers(5)
                .maxUsers(5)
                .build();

        when(roomRepository.findByRoomCodeAndDeletedAtIsNull("ROOM123")).thenReturn(Optional.of(room));
        when(pinGeneratorService.validatePin("1234", room.getPinHash())).thenReturn(true);

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);

        assertThatThrownBy(() -> roomService.joinRoom(request, user, httpRequest))
                .isInstanceOf(RoomFullException.class)
                .hasMessageContaining("La sala está llena");
    }
}
