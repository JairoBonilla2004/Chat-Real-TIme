package ec.edu.espe.chat_real_time.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.WebRequest;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        webRequest = Mockito.mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/test");
    }

    @Test
    void handleResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("not found");
        ResponseEntity<?> resp = handler.handleResourceNotFoundException(ex, webRequest);
        assertThat(resp.getStatusCodeValue()).isEqualTo(404);
        assertThat(((ec.edu.espe.chat_real_time.dto.response.ErrorResponse)resp.getBody()).getMessage()).isEqualTo("not found");
    }

    @Test
    void handleBadRequest() {
        BadRequestException ex = new BadRequestException("bad");
        ResponseEntity<?> resp = handler.handleBadRequestException(ex, webRequest);
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    void handleUnauthorized() {
        UnauthorizedException ex = new UnauthorizedException("no access");
        ResponseEntity<?> resp = handler.handleUnauthorizedException(ex, webRequest);
        assertThat(resp.getStatusCodeValue()).isEqualTo(401);
    }

    @Test
    void handleRoomFull() {
        RoomFullException ex = new RoomFullException("full");
        ResponseEntity<?> resp = handler.handleRoomFullException(ex, webRequest);
        assertThat(resp.getStatusCodeValue()).isEqualTo(409);
    }

    @Test
    void handleValidationExceptions() throws Exception {
        // mock a MethodArgumentNotValidException-ish by using BindException
        BindingResult br = Mockito.mock(BindingResult.class);
        when(br.getAllErrors()).thenReturn(Arrays.asList(new FieldError("obj", "field", "must not be empty")));
        org.springframework.web.bind.MethodArgumentNotValidException ex = Mockito.mock(org.springframework.web.bind.MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(br);

        ResponseEntity<?> resp = handler.handleValidationExceptions(ex, webRequest);
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
        assertThat(((ec.edu.espe.chat_real_time.dto.response.ErrorResponse)resp.getBody()).getDetails()).isNotEmpty();
    }

    @Test
    void handleBadCredentialsAndUserNotFound() {
        BadCredentialsException bc = new BadCredentialsException("bad creds");
        ResponseEntity<?> r1 = handler.handleBadCredentialsException(bc, webRequest);
        assertThat(r1.getStatusCodeValue()).isEqualTo(401);

        UsernameNotFoundException un = new UsernameNotFoundException("no user");
        ResponseEntity<?> r2 = handler.handleUsernameNotFoundException(un, webRequest);
        assertThat(r2.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    void handleJwtExceptions() {
        MalformedJwtException mj = new MalformedJwtException("bad");
        ResponseEntity<?> r = handler.handleJwtException(mj, webRequest);
        assertThat(r.getStatusCodeValue()).isEqualTo(401);

        ExpiredJwtException ej = Mockito.mock(ExpiredJwtException.class);
        ResponseEntity<?> r2 = handler.handleExpiredJwtException(ej, webRequest);
        assertThat(r2.getStatusCodeValue()).isEqualTo(401);
    }

    @Test
    void handleMaxUploadSizeExceeded() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(1L);
        ResponseEntity<?> r = handler.handleMaxUploadSizeExceededException(ex, webRequest);
        assertThat(r.getStatusCodeValue()).isEqualTo(413);
    }

    @Test
    void handleGlobalException() {
        Exception ex = new Exception("oops");
        ResponseEntity<?> r = handler.handleGlobalException(ex, webRequest);
        assertThat(r.getStatusCodeValue()).isEqualTo(500);
    }
}

