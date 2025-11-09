package ec.edu.espe.chat_real_time.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class PinGeneratorImpl implements PinGenerator {

  private final PasswordEncoder passwordEncoder;
  private static final SecureRandom secureRandom = new SecureRandom();
  private static final String DIGITS = "0123456789";

  @Override
  public String generatePin(int length) {
    if (length < 4 || length > 10) {
      throw new IllegalArgumentException("La longitud del PIN debe estar entre 4 y 10 dígitos");
    }

    StringBuilder pin = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int randomIndex = secureRandom.nextInt(DIGITS.length());
      pin.append(DIGITS.charAt(randomIndex));
    }

    String generatedPin = pin.toString();
    return generatedPin;
  }

  @Override
  public boolean validatePin(String plainPin, String hashedPin) {
    return passwordEncoder.matches(plainPin, hashedPin);
  }
}