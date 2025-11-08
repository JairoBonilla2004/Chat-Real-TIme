package ec.edu.espe.chat_real_time.security.jwt;

import ec.edu.espe.chat_real_time.model.user.User;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.*;
@Slf4j
@Service
public class JwtService {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Getter
  @Value("${jwt.access-token.expiration}")
  private long accessTokenExpiration;

  @Getter
  @Value("${jwt.refresh-token.expiration}")
  private long refreshTokenExpiration;

  private SecretKey getSingninKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes());
  }
  public String buildAccessToken(
          Map<String, Object> extraClaims,
          UserDetails userDetails,
          Long expiration
  ) {
    User user = (User) userDetails;
    Map<String, Object> claims = new HashMap<>(extraClaims);
    claims.put("userId", user.getId());
    claims.put("username", user.getUsername());
    claims.put("authorities", user.getAuthorities()); // esto devuel

    // 👇 Agregamos información específica según el tipo de perfil
    if (user.getAdminProfile() != null) {
      claims.put("email", user.getAdminProfile().getEmail());
      claims.put("roleType", "ADMIN");
    } else if (user.getGuestProfile() != null) {
      claims.put("nickname", user.getGuestProfile().getNickname());
      claims.put("expiresAt", user.getGuestProfile().getExpiresAt().toString());
      claims.put("roleType", "GUEST");
    }

    return Jwts.builder()
            .setClaims(claims)
            .setSubject(user.getId().toString())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSingninKey(), SignatureAlgorithm.HS256)
            .compact();
  }




  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {// ejemplo con getSbbject: Function<Claims, T> claimsResolver = x -> x.getSubject(), a x se aplica claims
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    try {
      return Jwts.parser()
              .setSigningKey(getSingninKey())
              .build()
              .parseClaimsJws(token)
              .getBody();
    } catch (ExpiredJwtException e) {
      log.warn("JWT token has expired: {}", e.getMessage());
      throw e;
    } catch (UnsupportedJwtException e) {
      log.warn("Unsupported JWT token: {}", e.getMessage());
      throw e;
    } catch (MalformedJwtException e) {
      log.warn("Malformed JWT token: {}", e.getMessage());
      throw e;
    } catch (SecurityException e) {
      log.warn("Invalid JWT signature: {}", e.getMessage());
      throw e;
    } catch (IllegalArgumentException e) {
      log.warn("JWT token compact of handler are invalid: {}", e.getMessage());
      throw e;
    }
  }

  public String extractUsername(String token) {
    return extractClaim(token, claims -> claims.get("username", String.class));
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    try {
      final Long userId = extractUserIdSub(token);
      User user = (User) userDetails;
      return userId.equals(user.getId()) && !isTokenExpired(token);
    } catch (Exception e) {
      log.debug("Token validation failed: {}", e.getMessage());
      return false;
    }
  }

  public Long extractUserIdSub(String token) {
    return Long.valueOf(extractClaim(token, Claims::getSubject));
  }

  public boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public String generateAccessToken(UserDetails userDetails) {
    User user = (User) userDetails;
    Map<String, Object> extraClaims = new HashMap<>();
    extraClaims.put("type", "access");
    return buildAccessToken(extraClaims, userDetails, accessTokenExpiration);
  }
}
