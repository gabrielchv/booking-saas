package dev.gazerah.booking.auth;

import dev.gazerah.booking.common.TenantContext;
import dev.gazerah.booking.user.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            authenticate(header.substring(7), request);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private void authenticate(String token, HttpServletRequest request) {
        try {
            Claims claims = jwtService.parse(token);
            Long userId = longClaim(claims, "userId");
            Long tenantId = longClaim(claims, "tenantId");
            Role role = Role.valueOf(claims.get("role", String.class));

            UserPrincipal principal = new UserPrincipal(userId, tenantId, claims.getSubject(), role);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            TenantContext.set(tenantId);
        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
            TenantContext.clear();
        }
    }

    private static Long longClaim(Claims claims, String name) {
        Object value = claims.get(name);
        return value == null ? null : ((Number) value).longValue();
    }
}
