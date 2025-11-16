package ec.edu.espe.chat_real_time.repository;

import ec.edu.espe.chat_real_time.model.RefreshToken;
import ec.edu.espe.chat_real_time.model.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TestEntityManager em;

    private User createUser(String username) {
        User u = User.builder().username(username).password("pwd").build();
        em.persist(u);
        em.flush();
        return u;
    }

    @Test
    void findByToken_and_deleteByToken() {
        User u = createUser("ruser");
        RefreshToken rt = RefreshToken.builder()
                .token("tok123")
                .user(u)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .isActive(true)
                .userAgent("ua")
                .build();
        em.persistAndFlush(rt);

        Optional<RefreshToken> found = refreshTokenRepository.findByToken("tok123");
        assertTrue(found.isPresent());

        refreshTokenRepository.deleteByToken("tok123");
        em.flush();

        Optional<RefreshToken> after = refreshTokenRepository.findByToken("tok123");
        assertTrue(after.isEmpty());
    }

    @Test
    void revokeAllUserTokens_and_counts() {
        User u = createUser("countuser");
        RefreshToken a = RefreshToken.builder().token("a").user(u).expiryDate(LocalDateTime.now().plusDays(1)).isActive(true).userAgent("ua1").build();
        RefreshToken b = RefreshToken.builder().token("b").user(u).expiryDate(LocalDateTime.now().plusDays(1)).isActive(true).userAgent("ua2").build();
        em.persist(a);
        em.persist(b);
        em.flush();

        long countActive = refreshTokenRepository.countByUserIdAndIsActiveTrue(u.getId());
        assertEquals(2, countActive);

        refreshTokenRepository.revokeAllUserTokens(u.getId());
        em.flush();

        long countAfter = refreshTokenRepository.countByUserIdAndIsActiveTrue(u.getId());
        assertEquals(0, countAfter);
    }

    @Test
    void findActiveByUserIdAndUserAgent_returns_correct_optional() {
        User u = createUser("uagent");
        RefreshToken rt = RefreshToken.builder().token("ta").user(u).expiryDate(LocalDateTime.now().plusDays(1)).isActive(true).userAgent("myagent").build();
        em.persistAndFlush(rt);

        Optional<RefreshToken> opt = refreshTokenRepository.findActiveByUserIdAndUserAgent(u.getId(), "myagent");
        assertTrue(opt.isPresent());
        assertEquals("ta", opt.get().getToken());
    }
}
