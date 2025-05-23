package com.example.english.demo.configuration;
//cái này để lấy token từ cookies nhé xác định admin
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtCookieFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    private final JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter;

    public JwtCookieFilter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;

        this.grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Cấu hình trùng với SecurityConfig nếu cần
        this.grantedAuthoritiesConverter.setAuthorityPrefix("");
        this.grantedAuthoritiesConverter.setAuthoritiesClaimName("scope");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = null;

            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("jwt".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }

            if (token != null) {
                try {
                    Jwt jwt = jwtDecoder.decode(token);
                    var authorities = grantedAuthoritiesConverter.convert(jwt);
                    var authToken = new JwtAuthenticationToken(jwt, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } catch (Exception ex) {
                    SecurityContextHolder.clearContext();
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
