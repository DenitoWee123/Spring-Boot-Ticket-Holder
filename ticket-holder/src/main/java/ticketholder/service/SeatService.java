package ticketholder.service;

import org.springframework.stereotype.Service;
import ticketholder.model.Seat;
import ticketholder.model.SeatStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class SeatService {

    private final ConcurrentHashMap<Integer, Seat> seats = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, ReentrantLock> locks = new ConcurrentHashMap<>();


    public void initSeats(int count) {
        for (int i = 1; i <= count; i++) {
            int id = i;
            seats.putIfAbsent(id, new Seat(id));
            locks.putIfAbsent(id, new ReentrantLock());
        }
    }

    /** Връща място по ID, ако съществува. */
    public Optional<Seat> getSeat(int seatId) {
        return Optional.ofNullable(seats.get(seatId));
    }

    /** Връща всички места. */
    public List<Seat> getAllSeats() {
        return new ArrayList<>(seats.values());
    }

    public HoldResult holdSeat(int seatId, String userId) {
        Seat seat = seats.get(seatId);
        if (seat == null) {
            return HoldResult.NOT_FOUND;
        }

        ReentrantLock lock = locks.computeIfAbsent(seatId, k -> new ReentrantLock());
        lock.lock();
        try {
            Instant now = Instant.now();

            // Ако е HELD, но е изтекло -> освобождаваме
            if (seat.isHoldExpired(now)) {
                seat.release();
            }

            if (seat.getStatus() == SeatStatus.SOLD) {
                return HoldResult.ALREADY_SOLD;
            }

            if (seat.getStatus() == SeatStatus.HELD) {
                return HoldResult.ALREADY_HELD;
            }

            // AVAILABLE -> HELD
            long FIXED_TTL_SECONDS = 600; // 10 минути

            Instant expiresAt = now.plusSeconds(FIXED_TTL_SECONDS);
            seat.hold(userId, expiresAt);
            return HoldResult.OK;

        } finally {
            lock.unlock();
        }
    }

    public ConfirmResult confirmPurchase(int seatId, String userId) {
        Seat seat = seats.get(seatId);
        if (seat == null) {
            return ConfirmResult.NOT_FOUND;
        }

        ReentrantLock lock = locks.computeIfAbsent(seatId, k -> new ReentrantLock());
        lock.lock();
        try {
            Instant now = Instant.now();

            if (seat.isHoldExpired(now)) {
                seat.release();
                return ConfirmResult.HOLD_EXPIRED;
            }

            if (seat.getStatus() == SeatStatus.SOLD) {
                return ConfirmResult.ALREADY_SOLD;
            }

            if (seat.getStatus() != SeatStatus.HELD) {
                return ConfirmResult.NOT_HELD;
            }

            if (seat.getHeldBy() == null || !seat.getHeldBy().equals(userId)) {
                return ConfirmResult.NOT_OWNER;
            }

            seat.sell();
            return ConfirmResult.OK;
        } finally {
            lock.unlock();
        }
    }

    public int releaseExpiredHolds() {
        Instant now = Instant.now();
        int released = 0;

        for (Seat seat : seats.values()) {
            int seatId = seat.getId();
            ReentrantLock lock = locks.computeIfAbsent(seatId, k -> new ReentrantLock());

            if (lock.tryLock()) {
                try {
                    if (seat.isHoldExpired(now)) {
                        seat.release();
                        released++;
                    }
                } finally {
                    lock.unlock();
                }
            }
        }

        return released;
    }

    public enum HoldResult {
        OK,
        NOT_FOUND,
        ALREADY_HELD,
        ALREADY_SOLD
    }

    public enum ConfirmResult {
        OK,
        NOT_FOUND,
        NOT_HELD,
        NOT_OWNER,
        HOLD_EXPIRED,
        ALREADY_SOLD
    }
}
