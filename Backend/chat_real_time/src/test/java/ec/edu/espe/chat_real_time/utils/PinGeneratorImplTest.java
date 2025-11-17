package ec.edu.espe.chat_real_time.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {PinGeneratorImpl.class})
@ExtendWith(SpringExtension.class)
class PinGeneratorImplTest {

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PinGenerator pinGenerator;

    @Test
    void generatePin_returnsDigitsOfRequestedLength() {
        String pin4 = pinGenerator.generatePin(4);
        assertThat(pin4).hasSize(4).matches("\\d+");

        String pin10 = pinGenerator.generatePin(10);
        assertThat(pin10).hasSize(10).matches("\\d+");
    }

    @Test
    void generatePin_invalidLengths_throwException() {
        assertThatThrownBy(() -> pinGenerator.generatePin(3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("longitud del PIN");

        assertThatThrownBy(() -> pinGenerator.generatePin(11))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("longitud del PIN");
    }

    @Test
    void validatePin_delegatesToPasswordEncoder() {
        when(passwordEncoder.matches("1234", "$hashed")).thenReturn(true);
        when(passwordEncoder.matches("0000", "$hashed")).thenReturn(false);

        assertThat(pinGenerator.validatePin("1234", "$hashed")).isTrue();
        assertThat(pinGenerator.validatePin("0000", "$hashed")).isFalse();
    }
}
