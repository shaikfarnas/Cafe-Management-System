/*import com.inn.cafe.JWT.CustomerUsersDetailsService;
import com.inn.cafe.JWT.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private Claims claims; // Instance variable for claims
    private String userName; // Instance variable for userName

    @Autowired
    private CustomerUsersDetailsService service;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        claims = null; // Reset claims
        userName = null; // Reset userName

        // Skip the filter for public endpoints like login, signup, and forgot password
        if (request.getRequestURI().matches("/user/login|/user/forgotPassword|/user/signup")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get Authorization header from request
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Extract token from header
            try {
                userName = jwtUtil.extractUsername(token); // Extract username
                claims = jwtUtil.extractAllClaims(token); // Extract claims
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or malformed token");
                return; // Stop the filter chain if token is invalid
            }
        }

        // If userName is not null and authentication is not yet set, set the authentication
        if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = service.loadUserByUsername(userName);
            if (jwtUtil.validateToken(token, userDetails)) {
                // Create authentication token and set it in SecurityContext
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid JWT token");
                return; // Stop the filter chain if token is invalid
            }
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }

    // Check if the user has 'admin' role
    public boolean isAdmin() {
        return claims != null && "admin".equalsIgnoreCase((String) claims.get("role"));
    }

    // Check if the user has 'user' role
    public boolean isUser() {
        return claims != null && "user".equalsIgnoreCase((String) claims.get("role"));
    }

    // Get the current logged-in username
    public String getCurrentUser() {
        return userName;
    }
}*/
package com.inn.cafe.JWT;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private Claims claims; // Instance variable for claims
    private String userName; // Instance variable for userName

    @Autowired
    private CustomerUsersDetailsService service;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        claims = null; // Reset claims
        userName = null; // Reset userName

        // Skip the filter for public endpoints like login, signup, and forgot password
        if (request.getRequestURI().matches("/user/login|/user/forgotPassword|/user/signup")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get Authorization header from request
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Extract token from header
            try {
                userName = jwtUtil.extractUsername(token); // Extract username
                claims = jwtUtil.extractAllClaims(token); // Extract claims
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or malformed token");
                return; // Stop the filter chain if token is invalid
            }
        }

        // If userName is not null and authentication is not yet set, set the authentication
        if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = service.loadUserByUsername(userName);
            if (jwtUtil.validateToken(token, userDetails)) {
                // Create authentication token and set it in SecurityContext
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid JWT token");
                return; // Stop the filter chain if token is invalid
            }
        }

        // Debugging logs (added for better understanding)
        if (claims != null) {
            System.out.println("JWT Claims: " + claims.toString());
            System.out.println("Logged-in User Role: " + claims.get("role"));
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }

    // Check if the user has 'admin' role
    public boolean isAdmin() {
        return claims != null && "admin".equalsIgnoreCase((String) claims.get("role"));
    }

    // Check if the user has 'user' role
    public boolean isUser() {
        return claims != null && "user".equalsIgnoreCase((String) claims.get("role"));
    }

    // Get the current logged-in username
    public String getCurrentUser() {
        return userName;
    }
}

