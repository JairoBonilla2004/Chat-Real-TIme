package ec.edu.espe.chat_real_time.repository;

import ec.edu.espe.chat_real_time.model.room.Room;
import ec.edu.espe.chat_real_time.model.room.RoomType;
import ec.edu.espe.chat_real_time.model.user.User;
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
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private TestEntityManager em;

    private User createUser(String username) {
        User u = User.builder().username(username).password("pwd").build();
        em.persist(u);
        em.flush();
        return u;
    }

    @Test
    void findByRoomCode_and_existsByRoomCode() {
        User creator = createUser("creator1");
        Room r = Room.builder().roomCode("RC1").name("Room 1").pinHash("ph").creator(creator).build();
        em.persistAndFlush(r);

        Optional<Room> opt = roomRepository.findByRoomCode("RC1");
        assertTrue(opt.isPresent());
        assertEquals("Room 1", opt.get().getName());

        assertTrue(roomRepository.existsByRoomCode("RC1"));
    }

    @Test
    void findByCreatorAndActive_and_countByCreator() {
        User creator = createUser("creator2");
        Room r1 = Room.builder().roomCode("RC2").name("R2").pinHash("p2").creator(creator).build();
        Room r2 = Room.builder().roomCode("RC3").name("R3").pinHash("p3").creator(creator).build();
        em.persist(r1);
        em.persist(r2);
        em.flush();

        List<Room> list = roomRepository.findByCreatorAndDeletedAtIsNull(creator);
        assertTrue(list.size() >= 2);

        long count = roomRepository.countByCreator(creator);
        assertTrue(count >= 2);
    }
}
