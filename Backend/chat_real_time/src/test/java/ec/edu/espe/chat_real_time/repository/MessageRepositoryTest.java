package ec.edu.espe.chat_real_time.repository;

import ec.edu.espe.chat_real_time.model.message.Message;
import ec.edu.espe.chat_real_time.model.message.MessageType;
import ec.edu.espe.chat_real_time.model.room.Room;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.model.user.UserSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private TestEntityManager em;

    private User createUser(String username) {
        User u = User.builder().username(username).password("pwd").build();
        em.persist(u);
        em.flush();
        return u;
    }

    private Room createRoom(User creator, String code) {
        Room r = Room.builder().roomCode(code).name("room").pinHash("p").creator(creator).build();
        em.persist(r);
        em.flush();
        return r;
    }

    @Test
    void findByRoomOrderAndCountAndRecent() {
        User u = createUser("muser");
        Room room = createRoom(u, "RMSG1");

        UserSession session = UserSession.builder().deviceId("d1").user(u).room(room).build();
        em.persistAndFlush(session);

        Message m1 = Message.builder().content("hi").user(u).room(room).session(session).messageType(MessageType.TEXT).sentAt(LocalDateTime.now().minusMinutes(5)).build();
        Message m2 = Message.builder().content("hi2").user(u).room(room).session(session).messageType(MessageType.TEXT).sentAt(LocalDateTime.now()).build();
        em.persist(m1);
        em.persist(m2);
        em.flush();

        List<Message> list = messageRepository.findByRoomOrderBySentAtDesc(room);
        assertTrue(list.size() >= 2);

        long count = messageRepository.countByRoom(room);
        assertEquals(2, count);

        List<Message> recent = messageRepository.findRecentMessages(room, LocalDateTime.now().minusMinutes(10));
        assertTrue(recent.size() >= 2);

        // paged
        var page = messageRepository.findByRoomAndIsDeletedFalseOrderBySentAtDesc(room, PageRequest.of(0, 10));
        assertTrue(page.getTotalElements() >= 2);
    }
}
