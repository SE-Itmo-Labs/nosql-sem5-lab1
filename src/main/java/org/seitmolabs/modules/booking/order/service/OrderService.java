package org.seitmolabs.modules.booking.order.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.Instant;

import org.seitmolabs.common.exceptions.BadRequestException;
import org.seitmolabs.common.exceptions.ConflictException;
import org.seitmolabs.modules.booking.hold.domain.SeatHold;
import org.seitmolabs.modules.booking.hold.service.SeatHoldService;
import org.seitmolabs.modules.booking.order.domain.CinemaOrder;
import org.seitmolabs.modules.booking.order.domain.OrderProduct;
import org.seitmolabs.modules.booking.order.domain.OrderStatus;
import org.seitmolabs.modules.booking.order.domain.Ticket;
import org.seitmolabs.modules.booking.order.domain.TicketStatus;
import org.seitmolabs.modules.booking.order.dto.CreateOrderRequest;
import org.seitmolabs.modules.booking.order.dto.OrderProductRequest;
import org.seitmolabs.modules.booking.order.dto.OrderResponse;
import org.seitmolabs.modules.booking.order.factory.OrderFactory;
import org.seitmolabs.modules.booking.order.mapper.OrderResponseMapper;
import org.seitmolabs.modules.booking.order.repository.CinemaOrderRepository;
import org.seitmolabs.modules.booking.order.repository.OrderProductRepository;
import org.seitmolabs.modules.booking.order.repository.TicketRepository;
import org.seitmolabs.modules.catalog.screening.domain.Screening;
import org.seitmolabs.modules.catalog.screening.domain.ScreeningStatus;
import org.seitmolabs.modules.catalog.screening.repository.ScreeningRepository;
import org.seitmolabs.modules.catalog.seat.domain.Seat;
import org.seitmolabs.modules.catalog.seat.repository.SeatRepository;
import org.seitmolabs.modules.product.domain.Product;
import org.seitmolabs.modules.product.repository.ProductRepository;
import org.seitmolabs.modules.notification.outbox.service.OutboxEventService;
import org.seitmolabs.modules.user.domain.User;
import org.seitmolabs.modules.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CinemaOrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final SeatHoldService seatHoldService;
    private final SeatOrderLockService lockService;
    private final OrderFactory orderFactory;
    private final OrderResponseMapper responseMapper;
    private final OutboxEventService outboxEventService;
    private final TransactionTemplate transactionTemplate;

    public OrderResponse confirm(CreateOrderRequest request, User currentUser) {
        CinemaOrder existing = orderRepository
                .findByUserIdAndIdempotencyKey(currentUser.getId(), request.idempotencyKey())
                .orElse(null);
        if (existing != null) {
            return getResponse(existing);
        }

        SeatHold hold = seatHoldService.requireOwned(request.holdId(), currentUser);
        try {
            OrderResponse response = lockService.execute(
                    hold.screeningId(),
                    hold.seatIds(),
                    () -> transactionTemplate.execute(status -> createOrder(request, currentUser, hold))
            );
            seatHoldService.releaseAfterConfirmation(hold);
            return response;
        } catch (DataIntegrityViolationException exception) {
            return orderRepository
                    .findByUserIdAndIdempotencyKey(currentUser.getId(), request.idempotencyKey())
                    .map(this::getResponse)
                    .orElseThrow(() -> new ConflictException("Одно или несколько мест уже куплены"));
        }
    }

    public List<OrderResponse> getOwnOrders(User user) {
        return orderRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::getResponse)
                .toList();
    }

    public OrderResponse getOwnOrder(Long orderId, User user) {
        CinemaOrder order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден: " + orderId));
        return getResponse(order);
    }

    public OrderResponse cancelOwnOrder(Long orderId, User user) {
        return transactionTemplate.execute(status -> {
            CinemaOrder order = orderRepository.findForUpdate(orderId, user.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Заказ не найден: " + orderId));
            if (order.getStatus() == OrderStatus.CANCELLED) {
                return getResponse(order);
            }
            List<Ticket> tickets = ticketRepository.findAllForOrder(orderId);
            Instant cancellationDeadline = Instant.now().plus(Duration.ofMinutes(30));
            if (tickets.stream().anyMatch(ticket -> !ticket.getScreening().getStartsAt()
                    .isAfter(cancellationDeadline))) {
                throw new ConflictException("Заказ можно отменить не позднее чем за 30 минут до сеанса");
            }
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelledAt(Instant.now());
            tickets.forEach(ticket -> ticket.setStatus(TicketStatus.CANCELLED));
            ticketRepository.saveAll(tickets);
            orderRepository.save(order);
            outboxEventService.enqueueOrderCancelled(order);
            return responseMapper.toResponse(order, tickets,
                    orderProductRepository.findAllForOrder(order.getId()));
        });
    }

    private OrderResponse createOrder(CreateOrderRequest request, User currentUser, SeatHold hold) {
        CinemaOrder existing = orderRepository
                .findByUserIdAndIdempotencyKey(currentUser.getId(), request.idempotencyKey())
                .orElse(null);
        if (existing != null) {
            return getResponse(existing);
        }

        SeatHold activeHold = seatHoldService.requireOwned(request.holdId(), currentUser);
        if (!activeHold.screeningId().equals(hold.screeningId())
                || !activeHold.seatIds().equals(hold.seatIds())) {
            throw new ConflictException("Состав удержания изменился");
        }

        Screening screening = screeningRepository.findById(hold.screeningId())
                .orElseThrow(() -> new EntityNotFoundException("Сеанс не найден: " + hold.screeningId()));
        if (screening.getStatus() != ScreeningStatus.SCHEDULED
                || !screening.getStartsAt().isAfter(Instant.now())) {
            throw new ConflictException("Сеанс больше недоступен для оформления");
        }
        List<Seat> seats = seatRepository.findAllByIdIn(hold.seatIds());
        if (seats.size() != hold.seatIds().size()) {
            throw new BadRequestException("Состав удержания содержит неизвестные места");
        }
        if (ticketRepository.existsConfirmedByScreeningIdAndSeatIdIn(
                screening.getId(), hold.seatIds(), TicketStatus.CONFIRMED)) {
            throw new ConflictException("Одно или несколько мест уже куплены");
        }

        Map<Product, Integer> products = resolveProducts(request.products());
        BigDecimal ticketTotal = screening.getBasePrice().multiply(BigDecimal.valueOf(seats.size()));
        BigDecimal productTotal = products.entrySet().stream()
                .map(entry -> entry.getKey().getPrice().multiply(BigDecimal.valueOf(entry.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        User userReference = userRepository.getReferenceById(currentUser.getId());
        CinemaOrder order = orderRepository.save(orderFactory.create(
                userReference,
                hold,
                request.idempotencyKey(),
                ticketTotal.add(productTotal)
        ));
        List<Ticket> tickets = ticketRepository.saveAll(seats.stream()
                .map(seat -> orderFactory.createTicket(order, screening, seat))
                .toList());
        List<OrderProduct> orderProducts = orderProductRepository.saveAll(products.entrySet().stream()
                .map(entry -> orderFactory.createProduct(order, entry.getKey(), entry.getValue()))
                .toList());
        outboxEventService.enqueueOrderConfirmed(order, hold.seatIds());
        return responseMapper.toResponse(order, tickets, orderProducts);
    }

    private Map<Product, Integer> resolveProducts(List<OrderProductRequest> requests) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderProductRequest request : requests) {
            quantities.merge(request.productId(), request.quantity(), Integer::sum);
        }
        List<Product> products = productRepository.findAllById(quantities.keySet());
        if (products.size() != quantities.size() || products.stream().anyMatch(product -> !product.isAvailable())) {
            throw new ConflictException("Один или несколько товаров кинобара недоступны");
        }
        Map<Product, Integer> result = new LinkedHashMap<>();
        products.forEach(product -> result.put(product, quantities.get(product.getId())));
        return result;
    }

    private OrderResponse getResponse(CinemaOrder order) {
        return responseMapper.toResponse(
                order,
                ticketRepository.findAllForOrder(order.getId()),
                orderProductRepository.findAllForOrder(order.getId())
        );
    }
}
