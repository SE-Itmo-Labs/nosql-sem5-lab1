package org.seitmolabs.modules.auth.filters;

import java.io.IOException;

import org.seitmolabs.modules.auth.exceptions.UnauthorizedException;
import org.seitmolabs.modules.auth.service.AuthService;
import org.seitmolabs.modules.user.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor
public class SimpleAuthFilter extends OncePerRequestFilter {

    public static final String CURRENT_USER_ATTRIBUTE = "currentUser";

    private final AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Preflight-запрос браузера не содержит учётных данных.
        if ("OPTIONS".equals(request.getMethod()) || "/error".equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }

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

        try {
            User user = authService.authenticate(username, password);
            request.setAttribute(CURRENT_USER_ATTRIBUTE, user);
        } catch (UnauthorizedException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"status\":401,\"message\":\"Invalid credentials\"}");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
}
