package ec.edu.espe.chat_real_time.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTest {
  public static void main(String[] args) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    String encoded = encoder.encode("admin123");
    System.out.println(encoded);
  }
}