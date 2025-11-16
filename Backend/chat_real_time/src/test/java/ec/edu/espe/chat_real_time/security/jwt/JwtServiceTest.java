package ec.edu.espe.chat_real_time.security.jwt;

import ec.edu.espe.chat_real_time.model.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // clave suficientemente larga para HS256
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "0123456701234567012345670123456701234567012345670123456701234567");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 1000L * 60 * 60); // 1h
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 1000L * 60 * 60 * 24 * 7); // 7d
    }

    @AfterEach
    void tearDown() {
        // nothing
    }

    @Test
    void generate_and_validate_token() {
        User u = User.builder().id(42L).username("testuser").password("pwd").build();

        String token = jwtService.generateAccessToken(u);
        assertNotNull(token);

        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);

        Long sub = jwtService.extractUserIdSub(token);
        assertEquals(42L, sub);

        assertFalse(jwtService.isTokenExpired(token));
        assertTrue(jwtService.isTokenValid(token, u));
    }

    @Test
    void extractExpiration_returns_future_date() {
        User u = User.builder().id(1L).username("u").password("p").build();
        String token = jwtService.generateAccessToken(u);
        assertTrue(jwtService.extractExpiration(token).getTime() > System.currentTimeMillis());
    }
}

