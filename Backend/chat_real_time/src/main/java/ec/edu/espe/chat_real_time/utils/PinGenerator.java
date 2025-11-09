package ec.edu.espe.chat_real_time.utils;

public interface PinGenerator {
  String generatePin(int length);
  boolean validatePin(String plainPin, String hashedPin);
}
