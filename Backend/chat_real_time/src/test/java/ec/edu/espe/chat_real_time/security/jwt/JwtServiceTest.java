package ec.edu.espe.chat_real_time.security.jwt;

import ec.edu.espe.chat_real_time.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setup() {
        jwtService = new JwtService();
        // set a deterministic secret and expirations
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "0123456701234567012345670123456701234567012345670123456701234567");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 1000L * 60 * 60); // 1h
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 1000L * 60 * 60 * 24);
    }

    private User makeUser(long id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setPassword("pw");
        u.setRoles(Collections.singleton(new ec.edu.espe.chat_real_time.model.Role(1L, "ROLE_USER", null, null)));
        return u;
    }

    @Test
    void generateAndValidateAccessToken_happyPath() {
        User u = makeUser(42L, "alice");
        Map<String, Object> extras = new HashMap<>();
        extras.put("x", "y");

        String token = jwtService.buildAccessToken(extras, u, 1000L * 60 * 60);
        assertThat(token).isNotNull();

        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("alice");

        Long sub = jwtService.extractUserIdSub(token);
        assertThat(sub).isEqualTo(42L);

        assertThat(jwtService.isTokenValid(token, u)).isTrue();
    }

    @Test
    void expiredToken_detected() {
        User u = makeUser(1L, "bob");
        // generate a token that is already expired by using a negative expiration
        String token = jwtService.buildAccessToken(new HashMap<>(), u, -1000L);

        // extractExpiration/extractAllClaims will throw ExpiredJwtException for expired tokens
        assertThatThrownBy(() -> jwtService.extractExpiration(token))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);

        // isTokenValid should return false and handle exceptions internally
        assertThat(jwtService.isTokenValid(token, u)).isFalse();
    }

    @Test
    void malformedToken_throws() {
        String bad = "not.a.token";
        assertThatThrownBy(() -> jwtService.extractUsername(bad)).isInstanceOf(io.jsonwebtoken.MalformedJwtException.class);
    }
}
