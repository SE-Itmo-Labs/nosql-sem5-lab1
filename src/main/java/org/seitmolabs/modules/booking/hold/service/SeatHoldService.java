package org.seitmolabs.modules.booking.hold.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.seitmolabs.common.exceptions.BadRequestException;
import org.seitmolabs.common.exceptions.ConflictException;
import org.seitmolabs.modules.booking.hold.domain.SeatHold;
import org.seitmolabs.modules.booking.hold.dto.CreateSeatHoldRequest;
import org.seitmolabs.modules.booking.hold.dto.SeatHoldResponse;
import org.seitmolabs.modules.booking.hold.factory.SeatHoldFactory;
import org.seitmolabs.modules.booking.hold.repository.SeatHoldRepository;
import org.seitmolabs.modules.booking.order.domain.TicketStatus;
import org.seitmolabs.modules.booking.order.repository.TicketRepository;
import org.seitmolabs.modules.catalog.screening.domain.Screening;
import org.seitmolabs.modules.catalog.screening.domain.ScreeningStatus;
import org.seitmolabs.modules.catalog.screening.repository.ScreeningRepository;
import org.seitmolabs.modules.catalog.seat.domain.Seat;
import org.seitmolabs.modules.catalog.seat.repository.SeatRepository;
import org.seitmolabs.modules.user.domain.User;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatHoldService {

    private final SeatHoldRepository holdRepository;
    private final SeatHoldFactory holdFactory;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;

    public SeatHoldResponse create(CreateSeatHoldRequest request, User user) {
        Screening screening = requireBookableScreening(request.screeningId());
        List<Long> seatIds = request.seatIds().stream().distinct().sorted().toList();
        if (seatIds.size() != request.seatIds().size()) {
            throw new BadRequestException("Список мест содержит дубликаты");
        }

        List<Seat> seats = seatRepository.findAllByIdIn(seatIds);
        if (seats.size() != seatIds.size()
                || seats.stream().anyMatch(seat -> !seat.getAuditorium().getId()
                .equals(screening.getAuditorium().getId()))) {
            throw new BadRequestException("Выбраны места, не принадлежащие залу сеанса");
        }
        if (ticketRepository.existsConfirmedByScreeningIdAndSeatIdIn(
                screening.getId(), seatIds, TicketStatus.CONFIRMED)) {
            throw new ConflictException("Одно или несколько мест уже куплены");
        }

        SeatHold hold = holdFactory.create(user.getId(), screening.getId(), seatIds);
        if (!holdRepository.create(hold, holdFactory.holdDuration())) {
            throw new ConflictException("Одно или несколько мест уже удерживаются другим клиентом");
        }
        return toResponse(hold);
    }

    public SeatHoldResponse getOwn(String holdId, User user) {
        return toResponse(requireOwned(holdId, user));
    }

    public SeatHold requireOwned(String holdId, User user) {
        SeatHold hold = holdRepository.findById(holdId)
                .orElseThrow(() -> new EntityNotFoundException("Удержание не найдено или истекло: " + holdId));
        if (!hold.userId().equals(user.getId())) {
            throw new EntityNotFoundException("Удержание не найдено: " + holdId);
        }
        return hold;
    }

    public void releaseOwn(String holdId, User user) {
        holdRepository.release(requireOwned(holdId, user));
    }

    public void releaseAfterConfirmation(SeatHold hold) {
        holdRepository.release(hold);
    }

    private Screening requireBookableScreening(Long screeningId) {
        return screeningRepository.findById(screeningId)
                .filter(screening -> screening.getStatus() == ScreeningStatus.SCHEDULED)
                .filter(screening -> screening.getStartsAt().isAfter(Instant.now()))
                .orElseThrow(() -> new EntityNotFoundException("Доступный сеанс не найден: " + screeningId));
    }

    private SeatHoldResponse toResponse(SeatHold hold) {
        long remainingSeconds = Math.max(0, Duration.between(Instant.now(), hold.expiresAt()).toSeconds());
        return new SeatHoldResponse(
                hold.id(),
                hold.screeningId(),
                hold.seatIds(),
                hold.createdAt(),
                hold.expiresAt(),
                remainingSeconds,
                "ACTIVE"
        );
    }
}
