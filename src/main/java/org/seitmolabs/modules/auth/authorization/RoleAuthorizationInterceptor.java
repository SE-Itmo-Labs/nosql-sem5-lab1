package org.seitmolabs.modules.auth.authorization;

import org.seitmolabs.modules.auth.filters.SimpleAuthFilter;
import org.seitmolabs.modules.user.domain.User;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Enforces role annotations after authentication and before controller code. */
@Component
public class RoleAuthorizationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }

        RequireRole requirement = AnnotatedElementUtils.findMergedAnnotation(
                method.getMethod(),
                RequireRole.class
        );
        if (requirement == null) {
            requirement = AnnotatedElementUtils.findMergedAnnotation(
                    method.getBeanType(),
                    RequireRole.class
            );
        }
        if (requirement == null) {
            return true;
        }

        User user = (User) request.getAttribute(SimpleAuthFilter.CURRENT_USER_ATTRIBUTE);
        if (user == null || user.getRole() != requirement.value()) {
            throw new ForbiddenException("Недостаточно прав для выполнения операции");
        }
        return true;
    }
}
