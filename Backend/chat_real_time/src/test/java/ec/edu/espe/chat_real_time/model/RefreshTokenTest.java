package ec.edu.espe.chat_real_time.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenTest {

    @Test
    void onCreate_setsCreatedAt() {
        RefreshToken token = RefreshToken.builder()
                .token("abc")
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();

        assertNull(token.getCreatedAt());
        token.onCreate();
        assertNotNull(token.getCreatedAt());
    }

    @Test
    void revoke_setsIsActiveFalse() {
        RefreshToken token = RefreshToken.builder()
                .isActive(true)
                .build();
        assertTrue(token.getIsActive());
        token.revoke();
        assertFalse(token.getIsActive());
    }

    @Test
    void isExpired_returnsTrueWhenPastExpiry_andFalseOtherwise() {
        RefreshToken past = RefreshToken.builder()
                .expiryDate(LocalDateTime.now().minusMinutes(1))
                .build();
        RefreshToken future = RefreshToken.builder()
                .expiryDate(LocalDateTime.now().plusMinutes(60))
                .build();

        assertTrue(past.isExpired());
        assertFalse(future.isExpired());
    }
}

