package org.seitmolabs.modules.block.controller;

import org.seitmolabs.modules.auth.filters.SimpleAuthFilter;
import org.seitmolabs.modules.block.DistributedLockDemoService;
import org.seitmolabs.modules.block.dto.ExecuteLockRequest;
import org.seitmolabs.modules.block.dto.ExecuteLockResponse;
import org.seitmolabs.modules.user.domain.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/locks")
@RequiredArgsConstructor
@Tag(name = "Distributed locks", description = "Распределённая блокировка критической секции")
public class DistributedLockController {

    private final DistributedLockDemoService demoService;

    @Operation(summary = "Попытаться выполнить операцию под распределённой блокировкой")
    @PostMapping("/execute")
    public ExecuteLockResponse execute(
            @Valid @RequestBody ExecuteLockRequest request,
            @RequestAttribute(SimpleAuthFilter.CURRENT_USER_ATTRIBUTE) User currentUser
    ) {
        return demoService.execute(
                request.resourceKey(),
                currentUser.getUsername(),
                request.holdMillis()
        );
    }
}
