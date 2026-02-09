package ticketholder.model;

import java.time.Instant;

public class Seat {

    private final int id;

    private SeatStatus status = SeatStatus.AVAILABLE;

    private String heldBy;               // null ако не е HELD
    private Instant holdExpiresAt;       // null ако не е HELD

    public Seat(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public String getHeldBy() {
        return heldBy;
    }

    public Instant getHoldExpiresAt() {
        return holdExpiresAt;
    }

    /** Връща true ако мястото е HELD и времето е изтекло. */
    public boolean isHoldExpired(Instant now) {
        return status == SeatStatus.HELD
                && holdExpiresAt != null
                && holdExpiresAt.isBefore(now);
    }

    /** Задържа мястото за user до expiresAt. */
    public void hold(String userId, Instant expiresAt) {
        this.status = SeatStatus.HELD;
        this.heldBy = userId;
        this.holdExpiresAt = expiresAt;
    }

    /** Освобождава мястото (след изтичане или отказ). */
    public void release() {
        this.status = SeatStatus.AVAILABLE;
        this.heldBy = null;
        this.holdExpiresAt = null;
    }

    /** Финализира покупката. */
    public void sell() {
        this.status = SeatStatus.SOLD;
        this.heldBy = null;
        this.holdExpiresAt = null;
    }
}
