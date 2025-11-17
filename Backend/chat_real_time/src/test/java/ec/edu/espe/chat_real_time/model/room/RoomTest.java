package ec.edu.espe.chat_real_time.model.room;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoomTest {

    @Test
    void isFull_returnsTrueWhenCurrentUsersGeMaxUsers() {
        Room r = Room.builder().maxUsers(3).currentUsers(3).build();
        assertTrue(r.isFull());

        r.setCurrentUsers(4);
        assertTrue(r.isFull());
    }

    @Test
    void isFull_returnsFalseWhenLessThanMax() {
        Room r = Room.builder().maxUsers(10).currentUsers(2).build();
        assertFalse(r.isFull());
    }

    @Test
    void incrementAndDecrement_modifyCurrentUsersSafely() {
        Room r = Room.builder().currentUsers(1).maxUsers(5).build();
        r.incrementCurrentUsers();
        assertEquals(2, r.getCurrentUsers());
        r.decrementCurrentUsers();
        assertEquals(1, r.getCurrentUsers());
        r.decrementCurrentUsers();
        assertEquals(0, r.getCurrentUsers());
        // decrement should not go below 0
        r.decrementCurrentUsers();
        assertEquals(0, r.getCurrentUsers());
    }
}

