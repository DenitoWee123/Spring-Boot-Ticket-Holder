package ticketholder.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ticketholder.model.Seat;
import ticketholder.service.SeatService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/seats")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;

        // In-memory init: създаваме примерно 20 места при стартиране.
        // (Ако искаш друго число, смени 20.)
        this.seatService.initSeats(20);
    }

    /**
     * GET /seats
     * Връща всички места (като JSON).
     */
    @GetMapping
    public List<Seat> getAllSeats() {
        return seatService.getAllSeats();
    }

    /**
     * GET /seats/{id}
     * Връща конкретно място или 404.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Seat> getSeat(@PathVariable int id) {
        return seatService.getSeat(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * POST /seats/{id}/hold?userId=U1&ttl=30
     */
    private static final long HOLD_TTL_SECONDS = 600;
    @PostMapping("/{id}/hold")
    public ResponseEntity<?> holdSeat(@PathVariable int id,
                                      @RequestParam String userId){

        SeatService.HoldResult result = seatService.holdSeat(id, userId);

        return switch (result) {
            case OK -> ResponseEntity.ok(Map.of(
                    "seatId", id,
                    "status", "HELD",
                    "heldBy", userId,
                    "ttlSeconds", HOLD_TTL_SECONDS
            ));
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "SEAT_NOT_FOUND",
                    "seatId", id
            ));
            case ALREADY_HELD -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "SEAT_ALREADY_HELD",
                    "seatId", id
            ));
            case ALREADY_SOLD -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "SEAT_ALREADY_SOLD",
                    "seatId", id
            ));
        };
    }

    /**
     * POST /seats/{id}/confirm?userId=U1
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable int id,
                                     @RequestParam String userId) {

        SeatService.ConfirmResult result = seatService.confirmPurchase(id, userId);

        return switch (result) {
            case OK -> ResponseEntity.ok(Map.of(
                    "seatId", id,
                    "status", "SOLD",
                    "purchasedBy", userId
            ));
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "SEAT_NOT_FOUND",
                    "seatId", id
            ));
            case NOT_HELD -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "SEAT_NOT_HELD",
                    "seatId", id
            ));
            case NOT_OWNER -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "NOT_HOLD_OWNER",
                    "seatId", id
            ));
            case HOLD_EXPIRED -> ResponseEntity.status(HttpStatus.GONE).body(Map.of(
                    "error", "HOLD_EXPIRED",
                    "seatId", id
            ));
            case ALREADY_SOLD -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "SEAT_ALREADY_SOLD",
                    "seatId", id
            ));
        };
    }
}
