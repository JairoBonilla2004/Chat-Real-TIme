package ec.edu.espe.chat_real_time.exception;

public class RoomFullException extends RuntimeException {
  public RoomFullException(String message) {
    super(message);
  }
}