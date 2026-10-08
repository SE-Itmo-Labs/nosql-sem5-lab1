package org.seitmolabs.modules.consistency.controller;

import org.seitmolabs.modules.consistency.dto.ModeRequest;
import org.seitmolabs.modules.consistency.dto.ExperimentRequest;
import org.seitmolabs.modules.consistency.dto.ExperimentResponse;
import org.seitmolabs.modules.consistency.dto.NodesResponse;
import org.seitmolabs.modules.consistency.dto.ReadResponse;
import org.seitmolabs.modules.consistency.dto.ReplicaActionResponse;
import org.seitmolabs.modules.consistency.dto.StateResponse;
import org.seitmolabs.modules.consistency.dto.WriteRequest;
import org.seitmolabs.modules.consistency.dto.WriteResponse;
import org.seitmolabs.modules.consistency.service.ConsistencyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/consistency")
@RequiredArgsConstructor
@Tag(name = "Redis consistency", description = "Исследование чтения и записи primary/replica")
public class ConsistencyController {

    private final ConsistencyService consistencyService;

    @Operation(summary = "Получить выбранные режимы")
    @GetMapping("/state")
    public StateResponse getState() {
        return consistencyService.getState();
    }

    @Operation(summary = "Изменить режим чтения или записи")
    @PutMapping("/mode")
    public StateResponse changeMode(@RequestBody ModeRequest request) {
        return consistencyService.changeMode(request);
    }

    @Operation(summary = "Записать экспериментальное значение")
    @PostMapping("/write")
    public WriteResponse write(@Valid @RequestBody WriteRequest request) {
        return consistencyService.write(request);
    }

    @Operation(summary = "Провести один эксперимент согласованности")
    @PostMapping("/experiments")
    public ExperimentResponse runExperiment(@Valid @RequestBody ExperimentRequest request) {
        return consistencyService.runExperiment(request);
    }

    @Operation(summary = "Прочитать значение в выбранном режиме")
    @GetMapping("/read/{key}")
    public ReadResponse read(@PathVariable String key) {
        return consistencyService.read(key);
    }

    @Operation(summary = "Сравнить значение на трёх нодах")
    @GetMapping("/nodes/{key}")
    public NodesResponse getNodes(@PathVariable String key) {
        return consistencyService.getNodes(key);
    }

    @Operation(summary = "Отключить реплику от primary")
    @PostMapping("/replicas/{replicaNumber}/detach")
    public ReplicaActionResponse detachReplica(@PathVariable int replicaNumber) {
        return consistencyService.detachReplica(replicaNumber);
    }

    @Operation(summary = "Подключить реплику обратно к primary")
    @PostMapping("/replicas/{replicaNumber}/attach")
    public ReplicaActionResponse attachReplica(@PathVariable int replicaNumber) {
        return consistencyService.attachReplica(replicaNumber);
    }
}
