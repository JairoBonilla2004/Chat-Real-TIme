// java
package ec.edu.espe.chat_real_time.repository;

import ec.edu.espe.chat_real_time.model.Attachment;
import ec.edu.espe.chat_real_time.model.message.Message;
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
class AttachmentRepositoryTest {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    void findByMessage_findByMessageId_and_findByFileName() {
        // crear y persistir usuario
        User u = User.builder().username("uatt").password("p").build();
        em.persist(u);

        // crear y persistir room
        Room room = Room.builder().roomCode("RATT").name("r").pinHash("p").creator(u).build();
        em.persist(room);

        // crear y persistir session
        UserSession session = UserSession.builder().deviceId("d").user(u).room(room).build();
        em.persist(session);

        // crear y persistir message (asegurar la relación con user, room y session)
        Message m = Message.builder().content("c").user(u).room(room).session(session).build();
        em.persist(m);

        // crear y persistir attachment relacionado con el message
        Attachment at = Attachment.builder()
                .fileName("file1")
                .originalFileName("orig")
                .filePath("/p")
                .fileUrl("/u")
                .message(m)
                .build();
        em.persistAndFlush(at); // flush para que esté disponible en queries

        // comprobar findByMessage
        List<Attachment> listByMessage = attachmentRepository.findByMessage(m);
        assertNotNull(listByMessage);
        assertTrue(listByMessage.stream().anyMatch(a -> "file1".equals(a.getFileName())));

        // comprobar findByMessageId
        List<Attachment> listByMessageId = attachmentRepository.findByMessageId(m.getId());
        assertNotNull(listByMessageId);
        assertTrue(listByMessageId.stream().anyMatch(a -> "file1".equals(a.getFileName())));

        // comprobar findByFileName
        Optional<Attachment> opt = attachmentRepository.findByFileName("file1");
        assertTrue(opt.isPresent());
        assertEquals("/u", opt.get().getFileUrl());
    }
}
