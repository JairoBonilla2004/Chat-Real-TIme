package ec.edu.espe.chat_real_time.security.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationEntryPointTest {

    @Test
    void commence_writes_json_error() throws Exception {
        JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        AuthenticationException ex = new AuthenticationException("Not authenticated") {};

        entryPoint.commence(request, response, ex);

        assertEquals(401, response.getStatus());
        String content = response.getContentAsString();
        assertTrue(content.contains("Not authenticated"));
        assertTrue(content.contains("status"));
    }
}

