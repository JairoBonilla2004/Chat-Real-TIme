package com.espe.chat_real_time.model;


import com.espe.chat_real_time.model.message.Message;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_attachments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileAttachment { // entidad para los archivos adjuntos por ejemplo supongamos que un usuario envia una imagen o un pdf o cualquier otro tipo de archivo a traves del chat

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String originalFilename;

  @Column(nullable = false, length = 255)
  private String storedFilename;

  @Column(nullable = false, length = 500)
  private String filePath;

  @Column(nullable = false, length = 100)
  private String contentType;

  @Column(nullable = false)
  private Long fileSize;

  @CreationTimestamp
  @Column(name = "uploaded_at", nullable = false, updatable = false)
  private LocalDateTime uploadedAt;

  // Relaciones
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "message_id", nullable = false)
  private Message message;

}