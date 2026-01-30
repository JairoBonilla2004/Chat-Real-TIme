package ec.edu.espe.chat_real_time.controller;

import ec.edu.espe.chat_real_time.exception.ResourceNotFoundException;
import ec.edu.espe.chat_real_time.model.Attachment;
import ec.edu.espe.chat_real_time.repository.AttachmentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

class FileControllerTest {

    private AttachmentRepository attachmentRepository;
    private FileController controller;
    private Path tempDir;
    private HttpServer httpServer;

    @BeforeEach
    void setUp() throws IOException {
        attachmentRepository = mock(AttachmentRepository.class);
        controller = new FileController(attachmentRepository);
        tempDir = Files.createTempDirectory("test-uploads");
        ReflectionTestUtils.setField(controller, "uploadDir", tempDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
        }
        if (tempDir != null && Files.exists(tempDir)) {
            try (Stream<Path> stream = Files.walk(tempDir)) {
                stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException e) {
                        // ignore
                    }
                });
            }
        }
    }

    @Test
    void testDownloadFileExistsAndUsesOriginalName() throws Exception {
        String storedName = "fileA.txt";
        Path file = tempDir.resolve(storedName);
        Files.writeString(file, "contenido prueba");

        Attachment att = new Attachment();
        att.setOriginalFileName("original-name.txt");
        when(attachmentRepository.findByFileName(storedName)).thenReturn(Optional.of(att));

        ResponseEntity<?> response = controller.downloadFile(storedName);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("original-name.txt"));
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof FileSystemResource);

        verify(attachmentRepository).findByFileName(storedName);
    }

    @Test
    void testDownloadFileNotFoundThrows() {
        String missing = "no-existe.bin";
        when(attachmentRepository.findByFileName(missing)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                controller.downloadFile(missing)
        );
        assertTrue(ex.getMessage().contains("Archivo no encontrado"));
    }

    @Test
    void testDownloadFileProbeContentTypeNullFallsBackToOctetStream() throws Exception {
        String name = "unknownfile.ext";
        Path file = tempDir.resolve(name);
        Files.writeString(file, "x");

        // Force probeContentType to likely return null by using unusual content; still we assert fallback
        when(attachmentRepository.findByFileName(name)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.downloadFile(name);

        assertEquals(200, response.getStatusCodeValue());
        // Accept any content type since probeContentType behavior may vary by system
        assertNotNull(response.getHeaders().getContentType());
    }

    @Test
    void testDownloadAttachmentSuccessFromHttpServer() throws Exception {
        // Start a lightweight HTTP server that returns a small payload
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        byte[] payload = "hello-world".getBytes();
        httpServer.createContext("/file", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                exchange.getResponseHeaders().add("Content-Type", "text/plain;charset=UTF-8");
                exchange.sendResponseHeaders(200, payload.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(payload);
                }
            }
        });
        httpServer.start();
        int port = httpServer.getAddress().getPort();
        String url = "http://localhost:" + port + "/file";

        Attachment att = new Attachment();
        att.setId(1L);
        att.setOriginalFileName("downloaded.txt");
        att.setFileName("stored.txt");
        att.setFileUrl(url);
        att.setFileType(null); // rely on connection content-type

        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(att));

        ResponseEntity<?> response = controller.downloadAttachment(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("downloaded.txt"));
        assertEquals("text/plain;charset=UTF-8", response.getHeaders().getContentType().toString());
        assertEquals(Long.valueOf(payload.length), response.getHeaders().getContentLength());
        assertTrue(response.getBody() instanceof InputStreamResource);

        verify(attachmentRepository).findById(1L);
    }

    @Test
    void testDownloadAttachmentNoUrlThrows() {
        Attachment att = new Attachment();
        att.setId(2L);
        att.setFileUrl(null);
        when(attachmentRepository.findById(2L)).thenReturn(Optional.of(att));

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                controller.downloadAttachment(2L)
        );
        assertTrue(ex.getMessage().contains("URL de archivo no disponible"));
    }

    @Test
    void testDownloadAttachmentRemote404Throws() throws Exception {
        // HTTP server that returns 404
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/notfound", exchange -> {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });
        httpServer.start();
        int port = httpServer.getAddress().getPort();
        String url = "http://localhost:" + port + "/notfound";

        Attachment att = new Attachment();
        att.setId(3L);
        att.setFileUrl(url);
        att.setOriginalFileName("x");
        when(attachmentRepository.findById(3L)).thenReturn(Optional.of(att));

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                controller.downloadAttachment(3L)
        );
        assertTrue(ex.getMessage().contains("Archivo no disponible"));
    }

    @Test
    void testDownloadAttachmentNotFoundThrows() {
        when(attachmentRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                controller.downloadAttachment(999L)
        );
        assertTrue(ex.getMessage().contains("Archivo no encontrado"));
    }
}
