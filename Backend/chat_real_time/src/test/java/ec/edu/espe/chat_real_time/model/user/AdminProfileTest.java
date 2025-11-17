package ec.edu.espe.chat_real_time.model.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminProfileTest {

    @Test
    void getFullName_combinesFirstAndLast() {
        AdminProfile a = AdminProfile.builder().firstName("Ana").lastName("Lopez").build();
        assertEquals("Ana Lopez", a.getFullName());
    }
}

