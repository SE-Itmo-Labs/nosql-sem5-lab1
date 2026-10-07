package org.seitmolabs.modules.auth.filters;

import java.io.IOException;

import org.seitmolabs.modules.auth.exceptions.UnauthorizedException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component 
public class SimpleAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Пропускаем авторизационные эндпоинты
        if (path.startsWith("/api/v1/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Пропускаем Swagger/документацию
        if (path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/swagger-ui.html") ||
                path.startsWith("/scalar.html") ||
                path.startsWith("/webjars")) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = request.getHeader("X-Username");
        String password = request.getHeader("X-Password");

        if (username == null || password == null ||
                !"admin".equals(username) || !"123".equals(password)) {
            throw new UnauthorizedException("Invalid credentials. Use X-Username: admin, X-Password: 123");
        }
        
        filterChain.doFilter(request, response);
    }
    
}
