package com.espe.chat_real_time.repository;

import com.espe.chat_real_time.model.Attachment;
import com.espe.chat_real_time.model.message.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
  List<Attachment> findByMessage(Message message);
  List<Attachment> findByMessageId(Long messageId);
}