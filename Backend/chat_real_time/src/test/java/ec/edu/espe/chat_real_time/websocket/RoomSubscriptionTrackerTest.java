package ec.edu.espe.chat_real_time.websocket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {RoomSubscriptionTracker.class})
@ExtendWith(SpringExtension.class)
class RoomSubscriptionTrackerTest {

    @Autowired
    private RoomSubscriptionTracker tracker;

    @Test
    void mapAndGetAndUnmapBehaveCorrectly() {
        tracker.map("s1", 100L);
        Optional<Long> r = tracker.getRoomId("s1");
        assertThat(r).isPresent();
        assertThat(r.get()).isEqualTo(100L);

        tracker.unmap("s1");
        assertThat(tracker.getRoomId("s1")).isEmpty();

        // null safety
        tracker.map(null, 1L);
        tracker.map("s2", null);
        assertThat(tracker.getRoomId(null)).isEmpty();
    }
}
