package ec.edu.espe.chat_real_time.Service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {ec.edu.espe.chat_real_time.Service.HttpRequestService.class})
@ExtendWith(SpringExtension.class)
class HttpRequestServiceTest {

    @Test
    void getClientIpAddress_checksHeadersAndRemoteAddr() {
        ec.edu.espe.chat_real_time.Service.HttpRequestService svc = new ec.edu.espe.chat_real_time.Service.HttpRequestService();
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Forwarded-For", "203.0.113.5, 198.51.100.1");
        String ip = svc.getClientIpAddress(req);
        assertThat(ip).isEqualTo("203.0.113.5");

        MockHttpServletRequest req2 = new MockHttpServletRequest();
        req2.setRemoteAddr("127.0.0.1");
        String ip2 = svc.getClientIpAddress(req2);
        assertThat(ip2).isEqualTo("127.0.0.1");
    }

    @Test
    void getUserAgent_andDeviceInfo() {
        ec.edu.espe.chat_real_time.Service.HttpRequestService svc = new ec.edu.espe.chat_real_time.Service.HttpRequestService();
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("User-Agent", "Mozilla/5.0 (iPhone) Mobile");
        assertThat(svc.getUserAgent(req)).isEqualTo("Mozilla/5.0 (iPhone) Mobile");
        assertThat(svc.getDeviceInfo(req)).isEqualTo("Mobile Device");

        MockHttpServletRequest req2 = new MockHttpServletRequest();
        req2.addHeader("User-Agent", "Some Tablet Agent TABLET");
        assertThat(svc.getDeviceInfo(req2)).isEqualTo("Tablet Device");

        MockHttpServletRequest req3 = new MockHttpServletRequest();
        req3.addHeader("User-Agent", "Java/1.8");
        assertThat(svc.getDeviceInfo(req3)).isEqualTo("Desktop Device");

        MockHttpServletRequest req4 = new MockHttpServletRequest();
        assertThat(svc.getUserAgent(req4)).isEqualTo("Unknown");
    }
}

