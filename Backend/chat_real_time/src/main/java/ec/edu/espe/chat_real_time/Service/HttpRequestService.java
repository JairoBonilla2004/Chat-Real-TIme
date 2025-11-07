package ec.edu.espe.chat_real_time.Service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class HttpRequestService {

  private static final String[] IP_HEADER_CANDIDATES = {// sirve para obtener la ip original del cliente detras de un proxy o load balancer
          "X-Forwarded-For",
          "Proxy-Client-IP",
          "WL-Proxy-Client-IP",
          "HTTP_X_FORWARDED_FOR",
          "HTTP_X_FORWARDED",
          "HTTP_X_CLUSTER_CLIENT_IP",
          "HTTP_CLIENT_IP",
          "HTTP_FORWARDED_FOR",
          "HTTP_FORWARDED",
          "HTTP_VIA",
          "REMOTE_ADDR"
  };

  public String getClientIpAddress(HttpServletRequest request){
    for (String header : IP_HEADER_CANDIDATES) {
      String ipAddress = request.getHeader(header);
      if (StringUtils.hasText(ipAddress) && !"unknown".equalsIgnoreCase(ipAddress)) {
        if(ipAddress.contains(",")){
          ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
      }
    }
    return request.getRemoteAddr(); // si no se encuentra en los headers, se obtiene la ip del request directamente es decir la del proxy o load balancer por lo que quiere decir que no se esta detras de uno ejemplo localhost
  }

  public String getUserAgent(HttpServletRequest request) {
    String userAgent = request.getHeader("User-Agent");
    return StringUtils.hasText(userAgent) ? userAgent : "Unknown";
  }

  public String getDeviceInfo(HttpServletRequest request){
    String userAgent = getUserAgent(request);
    if(userAgent.toLowerCase().contains("mobile")){
      return "Mobile Device";
    } else if(userAgent.toLowerCase().contains("tablet")){
      return "Tablet Device";
    } else {
      return "Desktop Device";
    }
  }


}
