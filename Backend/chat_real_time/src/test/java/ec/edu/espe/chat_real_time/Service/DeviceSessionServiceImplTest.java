package ec.edu.espe.chat_real_time.Service;

import ec.edu.espe.chat_real_time.Service.device.DeviceSessionServiceImpl;
import ec.edu.espe.chat_real_time.exception.BadRequestException;
import ec.edu.espe.chat_real_time.model.room.Room;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.model.user.UserSession;
import ec.edu.espe.chat_real_time.repository.UserSessionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeviceSessionServiceImplTest {

    private UserSessionRepository userSessionRepository;
    private DeviceSessionServiceImpl deviceSessionService;

    @BeforeEach
    void setUp() {
        userSessionRepository = mock(UserSessionRepository.class);
        deviceSessionService = new DeviceSessionServiceImpl(userSessionRepository);
    }


    @Test
    void validateUniqueSession_ShouldThrowIfSameDeviceActive() {
        User user = new User();
        String deviceId = "123";
        String ip = "10.0.0.1";

        UserSession active = new UserSession();
        active.setIsActive(true);
        active.setDeviceId(deviceId);
        active.setIpAddress(ip);

        when(userSessionRepository.findByUserAndIsActiveTrue(user))
                .thenReturn(List.of(active));

        assertThatThrownBy(() ->
                deviceSessionService.validateUniqueSession(user, deviceId, ip)
        ).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("sesión activa en esta sala desde este dispositivo");
    }


    @Test
    void validateUniqueSession_ShouldThrowIfDifferentDeviceActive() {
        User user = new User();

        UserSession active = new UserSession();
        active.setIsActive(true);
        active.setDeviceId("AAA");
        active.setIpAddress("1.1.1.1");

        when(userSessionRepository.findByUserAndIsActiveTrue(user))
                .thenReturn(List.of(active));

        assertThatThrownBy(() ->
                deviceSessionService.validateUniqueSession(user, "BBB", "2.2.2.2")
        ).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("sesión activa en esta sala desde otro dispositivo");
    }


    @Test
    void validateUniqueSession_ShouldAllowWhenNoActiveSessions() {
        User user = new User();

        when(userSessionRepository.findByUserAndIsActiveTrue(user))
                .thenReturn(List.of());

        assertThatCode(() ->
                deviceSessionService.validateUniqueSession(user, "X", "Y")
        ).doesNotThrowAnyException();
    }

    @Test
    void closeExistingSession_ShouldCloseActiveSessions() {
        User user = new User();

        Room room = mock(Room.class);

        UserSession session = new UserSession();
        session.setIsActive(true);
        session.setRoom(room);

        when(userSessionRepository.findByUserAndIsActiveTrue(user))
                .thenReturn(List.of(session));

        deviceSessionService.closeExistingSession(user);

        ArgumentCaptor<UserSession> captor = ArgumentCaptor.forClass(UserSession.class);
        verify(userSessionRepository, times(1)).save(captor.capture());

        UserSession saved = captor.getValue();

        assertThat(saved.getIsActive()).isFalse();
        assertThat(saved.getLeftAt()).isNotNull();


        verify(room, times(1)).decrementCurrentUsers();
    }


    @Test
    void generateDeviceFingerprint_ShouldGenerate32CharHash() {
        String fp = deviceSessionService.generateDeviceFingerprint("UA", "1.1.1.1");

        assertThat(fp).isNotNull();
        assertThat(fp.length()).isEqualTo(32);
    }
}
