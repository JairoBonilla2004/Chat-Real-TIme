package ec.edu.espe.chat_real_time.Service;

import ec.edu.espe.chat_real_time.Service.refreshToken.RefreshTokenServiceImpl;
import ec.edu.espe.chat_real_time.exception.InvalidTokenException;
import ec.edu.espe.chat_real_time.model.RefreshToken;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.repository.RefreshTokenRepository;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenServiceImpl service;

    private User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);

        // Simular valores @Value
        ReflectionTestUtils.setField(service, "refreshTokenExpiration", 60000L);
        ReflectionTestUtils.setField(service, "maxTokensPerUser", 5);
    }

    // ------------------------------------------------------------------------
    // CREATE REFRESH TOKEN
    // ------------------------------------------------------------------------
    @Test
    void testCreateRefreshToken_NewTokenCreated() {
        when(refreshTokenRepository.findActiveByUserIdAndUserAgent(anyLong(), anyString()))
                .thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RefreshToken token = service.createRefreshToken(
                user, "127.0.0.1", "Chrome", "Laptop"
        );

        assertNotNull(token.getToken());
        assertTrue(token.getIsActive());

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void testCreateRefreshToken_ReturnExistingActiveToken() {
        RefreshToken existing = RefreshToken.builder()
                .token("existing123")
                .isActive(true)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .user(user)
                .build();

        when(refreshTokenRepository.findActiveByUserIdAndUserAgent(anyLong(), anyString()))
                .thenReturn(Optional.of(existing));

        RefreshToken result = service.createRefreshToken(
                user, "127.0.0.1", "Chrome", "Laptop"
        );

        assertEquals("existing123", result.getToken());
        verify(refreshTokenRepository, never()).save(existing);
    }

    @Test
    void testCreateRefreshToken_ExpiredExistingIsRevoked() {
        RefreshToken expired = RefreshToken.builder()
                .token("expired")
                .isActive(true)
                .expiryDate(LocalDateTime.now().minusMinutes(1))
                .user(user)
                .build();

        when(refreshTokenRepository.findActiveByUserIdAndUserAgent(anyLong(), anyString()))
                .thenReturn(Optional.of(expired));

        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RefreshToken result = service.createRefreshToken(
                user, "127.0.0.1", "Chrome", "Laptop"
        );

        assertNotEquals("expired", result.getToken());
        assertFalse(expired.getIsActive());
        verify(refreshTokenRepository, times(2)).save(any());
    }

    // ------------------------------------------------------------------------
    // deleteExpiredTokensByUser
    // ------------------------------------------------------------------------
    @Test
    void testDeleteExpiredTokensByUser() {
        RefreshToken t1 = new RefreshToken();
        List<RefreshToken> expired = List.of(t1);

        when(refreshTokenRepository.findAllByUserIdAndExpiryDateBefore(anyLong(), any()))
                .thenReturn(expired);

        service.deleteExpiredTokensByUser(1L);

        verify(refreshTokenRepository).deleteAll(expired);
    }

    @Test
    void testDeleteExpiredTokensByUser_NoExpiredTokens() {
        when(refreshTokenRepository.findAllByUserIdAndExpiryDateBefore(anyLong(), any()))
                .thenReturn(List.of());

        service.deleteExpiredTokensByUser(1L);

        verify(refreshTokenRepository, never()).deleteAll(any());
    }


    @Test
    void testLimitTokenPerUser_RevokeOldestTokens() {

        ReflectionTestUtils.setField(service, "maxTokensPerUser", 2);

        RefreshToken t1 = RefreshToken.builder()
                .token("old1")
                .createdAt(LocalDateTime.now().minusMinutes(30))
                .isActive(true)
                .build();

        RefreshToken t2 = RefreshToken.builder()
                .token("old2")
                .createdAt(LocalDateTime.now().minusMinutes(20))
                .isActive(true)
                .build();

        RefreshToken t3 = RefreshToken.builder()
                .token("old3")
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .isActive(true)
                .build();

        when(refreshTokenRepository.findAllByUserIdAndIsActiveTrue(1L))
                .thenReturn(List.of(t1, t2, t3));

        service.limitTokenPerUser(1L);

        // Se revocan 2 tokens: los dos más antiguos
        assertFalse(t1.getIsActive()); // revocado
        assertFalse(t2.getIsActive()); // revocado

        // Solo el más reciente debe sobrevivir
        assertTrue(t3.getIsActive());

        verify(refreshTokenRepository, times(1)).saveAll(any());
    }



    @Test
    void testFindByToken_success() {
        RefreshToken token = new RefreshToken();
        when(refreshTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));

        RefreshToken result = service.findByToken("abc");
        assertEquals(token, result);
    }

    @Test
    void testFindByToken_notFound() {
        when(refreshTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThrows(
                InvalidTokenException.class,
                () -> service.findByToken("missing")
        );
    }

    // ------------------------------------------------------------------------
    // revokeToken
    // ------------------------------------------------------------------------
    @Test
    void testRevokeToken_success() {
        RefreshToken token = RefreshToken.builder()
                .token("abc")
                .isActive(true)
                .user(user)
                .build();

        when(refreshTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));

        service.revokeToken("abc");

        assertFalse(token.getIsActive());
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void testRevokeToken_alreadyInactive() {
        RefreshToken token = RefreshToken.builder()
                .token("abc")
                .isActive(false)
                .user(user)
                .build();

        when(refreshTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));

        service.revokeToken("abc");

        verify(refreshTokenRepository, never()).save(token);
    }

    // ------------------------------------------------------------------------
    // revokeAllUserTokens
    // ------------------------------------------------------------------------
    @Test
    void testRevokeAllUserTokens() {
        service.revokeAllUserTokens(1L);
        verify(refreshTokenRepository).revokeAllUserTokens(1L);
    }

    // ------------------------------------------------------------------------
    // deleteByToken
    // ------------------------------------------------------------------------
    @Test
    void testDeleteByToken() {
        service.deleteByToken("tok123");
        verify(refreshTokenRepository).deleteByToken("tok123");
    }

    // ------------------------------------------------------------------------
    // rotateRefreshToken
    // ------------------------------------------------------------------------
    @Test
    void testRotateRefreshToken() {

        RefreshToken oldToken = RefreshToken.builder()
                .user(user)
                .token("old")
                .isActive(true)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .build();

        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(refreshTokenRepository.findActiveByUserIdAndUserAgent(anyLong(), anyString()))
                .thenReturn(Optional.empty());

        RefreshToken newToken = service.rotateRefreshToken(
                oldToken, "127.0.0.1", "Chrome", "Laptop"
        );

        assertFalse(oldToken.getIsActive());
        assertNotNull(newToken);
        assertNotEquals("old", newToken.getToken());
        verify(refreshTokenRepository, times(2)).save(any());
    }
}
