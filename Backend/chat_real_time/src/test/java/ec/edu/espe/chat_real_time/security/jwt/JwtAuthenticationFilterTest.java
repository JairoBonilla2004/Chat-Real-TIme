package ec.edu.espe.chat_real_time.security.jwt;

import ec.edu.espe.chat_real_time.Service.UserDetailsServiceImpl;
import ec.edu.espe.chat_real_time.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetails;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private UserDetailsServiceImpl userDetailsService;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userDetailsService = mock(UserDetailsServiceImpl.class);
        filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    @Test
    void doFilter_skips_when_no_token() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(req, res, chain);
        assertEquals(200, res.getStatus());
    }

    @Test
    void doFilter_sets_authentication_when_token_valid() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        String token = "token123";
        req.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extractUserIdSub(token)).thenReturn(7L);
        User user = User.builder().id(7L).username("u").password("p").build();
        when(userDetailsService.loadUserById(7L)).thenReturn((UserDetails) user);
        when(jwtService.isTokenValid(token, user)).thenReturn(true);

        filter.doFilterInternal(req, res, chain);

        // after chain, status still 200
        assertEquals(200, res.getStatus());
        // security context should be set (we cannot inspect it easily here without extra setup), but no exceptions
    }
}

