package com.inn.cafe.JWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtUtil {
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Generates a new key for signing

    // Extract the username (subject) from the JWT token
    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    // Extract the expiration date from the JWT token
    public Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    // Extract any claim from the JWT token using a claimsResolver function
    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token); // Parse the token into claims
        return claimsResolver.apply(claims);
    }

    // Extract all claims from the JWT token
    public Claims extractAllClaims(String token) {
        try {
            // Try parsing the token to get claims
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY) // The signing key used for parsing the token
                    .build()
                    .parseClaimsJws(token)   // Parses the JWT string into claims
                    .getBody();
        } catch (Exception e) {
            // Catch any exceptions and provide a more descriptive error message
            throw new RuntimeException("Invalid or malformed JWT token: " + e.getMessage(), e);
        }
    }

    // Check if the token has expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date()); // Check if the token expiration time has passed
    }

    // Generate a new JWT token for a user based on email and role
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role) // Add role as a custom claim
                .setIssuedAt(new Date(System.currentTimeMillis())) // Issue time of the token
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // Token valid for 10 hours
                .signWith(SECRET_KEY) // Sign the token using the secret key
                .compact();
    }

    // Validate if the token is correct by comparing username and checking expiration
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token)); // Check if the username matches and token is not expired
    }
}
