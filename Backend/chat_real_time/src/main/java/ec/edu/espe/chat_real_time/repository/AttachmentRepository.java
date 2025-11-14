package ec.edu.espe.chat_real_time.repository;

import ec.edu.espe.chat_real_time.model.Attachment;
import ec.edu.espe.chat_real_time.model.message.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
  List<Attachment> findByMessage(Message message);
  List<Attachment> findByMessageId(Long messageId);
  Optional<Attachment> findByFileName(String fileName);
}