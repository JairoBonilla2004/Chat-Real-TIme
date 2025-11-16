package ec.edu.espe.chat_real_time.repository;

import ec.edu.espe.chat_real_time.model.room.Room;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.model.user.UserSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserSessionRepositoryTest {

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    void findByUserAndRoomAndActive_and_lists() {
        User u = User.builder().username("su").password("p").build();
        em.persist(u);
        Room r = Room.builder().roomCode("rs").name("n").pinHash("p").creator(u).build();
        em.persist(r);

        UserSession s = UserSession.builder().deviceId("d1").user(u).room(r).isActive(true).build();
        em.persistAndFlush(s);

        Optional<UserSession> opt = userSessionRepository.findByUserAndRoomAndIsActiveTrue(u, r);
        assertTrue(opt.isPresent());

        List<UserSession> byRoom = userSessionRepository.findByRoomAndIsActiveTrue(r);
        assertTrue(byRoom.size() >= 1);

        List<UserSession> byUser = userSessionRepository.findByUserAndIsActiveTrue(u);
        assertTrue(byUser.size() >= 1);
    }
}
