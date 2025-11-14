package ec.edu.espe.chat_real_time.controller;

import ec.edu.espe.chat_real_time.exception.ResourceNotFoundException;
import ec.edu.espe.chat_real_time.model.Attachment;
import ec.edu.espe.chat_real_time.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FileController {

  @Value("${file.upload-dir}")
  private String uploadDir;

  private final AttachmentRepository attachmentRepository;

  @GetMapping("/api/files/{fileName:.+}")
  public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
    try {
      Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
      if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
        throw new ResourceNotFoundException("Archivo no encontrado");
      }

      String contentType = Files.probeContentType(filePath);
      if (contentType == null) {
        contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
      }

      // Try to get the original file name from DB
      Optional<Attachment> attachmentOpt = attachmentRepository.findByFileName(fileName);
      String downloadName = attachmentOpt.map(Attachment::getOriginalFileName).orElse(fileName);

      Resource resource = new FileSystemResource(filePath);

      return ResponseEntity.ok()
              .contentType(MediaType.parseMediaType(contentType))
              .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadName + "\"")
              .body(resource);
    } catch (IOException ex) {
      log.error("Error al servir archivo {}", fileName, ex);
      throw new ResourceNotFoundException("Archivo no disponible");
    }
  }
}
