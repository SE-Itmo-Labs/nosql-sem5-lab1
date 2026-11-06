package org.seitmolabs.modules.booking.hold.controller;

import org.seitmolabs.modules.auth.filters.SimpleAuthFilter;
import org.seitmolabs.modules.booking.hold.dto.CreateSeatHoldRequest;
import org.seitmolabs.modules.booking.hold.dto.SeatHoldResponse;
import org.seitmolabs.modules.booking.hold.service.SeatHoldService;
import org.seitmolabs.modules.user.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/seat-holds")
@RequiredArgsConstructor
public class SeatHoldController {

    private final SeatHoldService seatHoldService;

    @PostMapping
    public ResponseEntity<SeatHoldResponse> create(
            @Valid @RequestBody CreateSeatHoldRequest request,
            @RequestAttribute(SimpleAuthFilter.CURRENT_USER_ATTRIBUTE) User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seatHoldService.create(request, user));
    }

    @GetMapping("/{holdId}")
    public SeatHoldResponse get(
            @PathVariable String holdId,
            @RequestAttribute(SimpleAuthFilter.CURRENT_USER_ATTRIBUTE) User user
    ) {
        return seatHoldService.getOwn(holdId, user);
    }

    @DeleteMapping("/{holdId}")
    public ResponseEntity<Void> release(
            @PathVariable String holdId,
            @RequestAttribute(SimpleAuthFilter.CURRENT_USER_ATTRIBUTE) User user
    ) {
        seatHoldService.releaseOwn(holdId, user);
        return ResponseEntity.noContent().build();
    }
}
