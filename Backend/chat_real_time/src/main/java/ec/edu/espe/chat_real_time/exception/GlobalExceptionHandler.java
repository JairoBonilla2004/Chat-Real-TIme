package ec.edu.espe.chat_real_time.exception;

import ec.edu.espe.chat_real_time.dto.response.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
          ResourceNotFoundException ex,
          WebRequest request
  ) {
    ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message(ex.getMessage())
            .error("Resource Not Found")
            .status(HttpStatus.NOT_FOUND.value())
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorResponse> handleBadRequestException(
          BadRequestException ex,
          WebRequest request
  ) {
    ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message(ex.getMessage())
            .error("Bad Request")
            .status(HttpStatus.BAD_REQUEST.value())
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorizedException(
          UnauthorizedException ex,
          WebRequest request
  ) {
    ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message(ex.getMessage())
            .error("Unauthorized")
            .status(HttpStatus.UNAUTHORIZED.value())
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  @ExceptionHandler(RoomFullException.class)
  public ResponseEntity<ErrorResponse> handleRoomFullException(
          RoomFullException ex,
          WebRequest request
  ) {
    ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message(ex.getMessage())
            .error("Room Full")
            .status(HttpStatus.CONFLICT.value())
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();

    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
          MethodArgumentNotValidException ex,
          WebRequest request
  ) {
    List<String> details = new ArrayList<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      details.add(fieldName + ": " + errorMessage);
    });

    ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message("Errores de validación")
            .error("Validation Error")
            .status(HttpStatus.BAD_REQUEST.value())
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .details(details)
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentialsException(
          BadCredentialsException ex,
          WebRequest request
  ) {
    ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message("Credenciales inválidas")
            .error("Authentication Failed")
            .status(HttpStatus.UNAUTHORIZED.value())
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(
          UsernameNotFoundException ex,
          WebRequest request
  ) {
    ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message("Usuario no encontrado")
            .error("User Not Found")
            .status(HttpStatus.NOT_FOUND.value())
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler({SignatureException.class, MalformedJwtException.class})
  public ResponseEntity<ErrorResponse> handleJwtException(
          Exception ex,
          WebRequest request
  ) {
    ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message("Token JWT inválido")
            .error("Invalid Token")
            .status(HttpStatus.UNAUTHORIZED.value())
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  @ExceptionHandler(ExpiredJwtException.class)
  public ResponseEntity<ErrorResponse> handleExpiredJwtException(
          ExpiredJwtException ex,
          WebRequest request
  ) {
    ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message("Token JWT expirado")
            .error("Token Expired")
            .status(HttpStatus.UNAUTHORIZED.value())
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
          MaxUploadSizeExceededException ex,
          WebRequest request
  ) {
    ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message("El archivo excede el tamaño máximo permitido")
            .error("File Too Large")
            .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();

    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(
          Exception ex,
          WebRequest request
  ) {
    ErrorResponse error = ErrorResponse.builder()
            .success(false)
            .message("Ha ocurrido un error interno en el servidor")
            .error("Internal Server Error")
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}