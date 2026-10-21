package org.seitmolabs.modules.consistency.controller;

import org.seitmolabs.modules.consistency.dto.ExperimentRequest;
import org.seitmolabs.modules.consistency.dto.ExperimentResponse;
import org.seitmolabs.modules.consistency.dto.ReplicaActionResponse;
import org.seitmolabs.modules.consistency.service.ConsistencyService;
import org.seitmolabs.modules.auth.authorization.RequireRole;
import org.seitmolabs.modules.user.enums.Role;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/laboratory/consistency")
@RequiredArgsConstructor
@RequireRole(Role.ROLE_ADMIN)
@Tag(name = "Redis consistency", description = "Исследование чтения и записи primary/replica")
public class ConsistencyController {

    private final ConsistencyService consistencyService;

    @Operation(summary = "Провести один эксперимент согласованности")
    @PostMapping("/experiments")
    public ExperimentResponse runExperiment(@Valid @RequestBody ExperimentRequest request) {
        return consistencyService.runExperiment(request);
    }

    @Operation(summary = "Отключить реплику от primary")
    @PostMapping("/nodes/{nodeId}/detach")
    public ReplicaActionResponse detachReplica(@PathVariable String nodeId) {
        return consistencyService.detachReplica(nodeId);
    }

    @Operation(summary = "Подключить реплику обратно к primary")
    @PostMapping("/nodes/{nodeId}/attach")
    public ReplicaActionResponse attachReplica(@PathVariable String nodeId) {
        return consistencyService.attachReplica(nodeId);
    }
}
