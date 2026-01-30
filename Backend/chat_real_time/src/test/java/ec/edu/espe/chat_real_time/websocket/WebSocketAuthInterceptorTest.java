package ec.edu.espe.chat_real_time.websocket;

import ec.edu.espe.chat_real_time.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketAuthInterceptorTest {

    @Mock
    JwtService jwtService;

    @Mock
    UserDetailsService userDetailsService;

    private RoomSubscriptionTracker tracker;
    private WebSocketAuthInterceptor interceptor;

    @BeforeEach
    void setUp() {
        tracker = new RoomSubscriptionTracker();
        interceptor = new WebSocketAuthInterceptor(jwtService, userDetailsService, tracker);
        SecurityContextHolder.clearContext();
    }

    @Test
    void subscribe_pattern1_mapsRoom() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(org.springframework.messaging.simp.stomp.StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/room/55/messages");
        accessor.setSessionId("sess1");
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        interceptor.preSend(message, mock(MessageChannel.class));

        assertThat(tracker.getRoomId("sess1")).isPresent().contains(55L);
    }

    @Test
    void subscribe_pattern2_mapsRoom() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(org.springframework.messaging.simp.stomp.StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/room/77/typing");
        accessor.setSessionId("sess2");
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        interceptor.preSend(message, mock(MessageChannel.class));

        assertThat(tracker.getRoomId("sess2")).isPresent().contains(77L);
    }

    @Test
    void subscribe_nonMatching_doesNotMap() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(org.springframework.messaging.simp.stomp.StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/other/1");
        accessor.setSessionId("sess3");
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        interceptor.preSend(message, mock(MessageChannel.class));

        assertThat(tracker.getRoomId("sess3")).isEmpty();
    }

    @Test
    void subscribe_nonNumericRoom_doesNotMap() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(org.springframework.messaging.simp.stomp.StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/room/abc/messages");
        accessor.setSessionId("sess4");
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        interceptor.preSend(message, mock(MessageChannel.class));

        assertThat(tracker.getRoomId("sess4")).isEmpty();
    }

    @Test
    void connect_withValidToken_setsSecurityContextAndAccessorUser() {
        String token = "validtoken";
        StompHeaderAccessor accessor = StompHeaderAccessor.create(org.springframework.messaging.simp.stomp.StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        accessor.addNativeHeader("Authorization", "Bearer " + token);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(jwtService.extractUsername(token)).thenReturn("u1");
        UserDetails ud = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("u1")).thenReturn(ud);
        when(jwtService.isTokenValid(token, ud)).thenReturn(true);
        when(ud.getAuthorities()).thenReturn(java.util.Collections.emptyList());

        Message<?> returned = interceptor.preSend(message, mock(MessageChannel.class));
        assertThat(returned).isNotNull();

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        StompHeaderAccessor resAccessor = MessageHeaderAccessor.getAccessor(returned, StompHeaderAccessor.class);
        assertThat(resAccessor).isNotNull();
        assertThat(resAccessor.getUser()).isNotNull();
    }

    @Test
    void connect_withInvalidToken_doesNotSetAuthentication() {
        String token = "invalidtoken";
        StompHeaderAccessor accessor = StompHeaderAccessor.create(org.springframework.messaging.simp.stomp.StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer " + token);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(jwtService.extractUsername(token)).thenReturn("u2");
        UserDetails ud = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("u2")).thenReturn(ud);
        when(jwtService.isTokenValid(token, ud)).thenReturn(false);

        Message<?> returned = interceptor.preSend(message, mock(MessageChannel.class));
        assertThat(returned).isNotNull();

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        // also assert that returned accessor has no user set
        StompHeaderAccessor resAccessor = MessageHeaderAccessor.getAccessor(returned, StompHeaderAccessor.class);
        assertThat(resAccessor).isNotNull();
        assertThat(resAccessor.getUser()).isNull();
    }

    @Test
    void connect_withExceptionDuringAuth_isHandledAndNoAuthSet() {
        String token = "badtoken";
        StompHeaderAccessor accessor = StompHeaderAccessor.create(org.springframework.messaging.simp.stomp.StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer " + token);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(jwtService.extractUsername(token)).thenThrow(new RuntimeException("boom"));

        Message<?> returned = interceptor.preSend(message, mock(MessageChannel.class));
        assertThat(returned).isNotNull();

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        StompHeaderAccessor resAccessor = MessageHeaderAccessor.getAccessor(returned, StompHeaderAccessor.class);
        assertThat(resAccessor).isNotNull();
        assertThat(resAccessor.getUser()).isNull();
    }

    @Test
    void connect_withoutAuthorization_doesNothing() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(org.springframework.messaging.simp.stomp.StompCommand.CONNECT);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> returned = interceptor.preSend(message, mock(MessageChannel.class));
        assertThat(returned).isNotNull();

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        StompHeaderAccessor resAccessor = MessageHeaderAccessor.getAccessor(returned, StompHeaderAccessor.class);
        assertThat(resAccessor).isNotNull();
        assertThat(resAccessor.getUser()).isNull();
    }
}
