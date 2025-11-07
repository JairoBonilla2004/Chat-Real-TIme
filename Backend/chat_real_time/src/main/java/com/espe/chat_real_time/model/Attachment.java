package com.espe.chat_real_time.model;


import com.espe.chat_real_time.model.message.Message;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "attachments", indexes = {
        @Index(name = "idx_attachment_message", columnList = "message_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "file_name", nullable = false, length = 255)
  private String fileName;

  @Column(name = "original_file_name", nullable = false, length = 255)
  private String originalFileName;

  @Column(name = "file_type", length = 50)
  private String fileType;

  @Column(name = "file_size")
  private Long fileSize;

  @Column(name = "file_path", nullable = false, length = 500)
  private String filePath;

  @Column(name = "file_url", nullable = false, length = 500)
  private String fileUrl;

  @CreationTimestamp
  @Column(name = "uploaded_at", nullable = false, updatable = false)
  private LocalDateTime uploadedAt;

  // Relaciones

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "message_id", nullable = false)
  private Message message;
}