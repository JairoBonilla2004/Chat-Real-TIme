package ec.edu.espe.chat_real_time.exception;

public class BadRequestException extends RuntimeException {
  public BadRequestException(String message) {
    super(message);
  }
}