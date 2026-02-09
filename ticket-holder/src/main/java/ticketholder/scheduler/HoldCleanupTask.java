package ticketholder.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ticketholder.service.SeatService;

/**
 * HoldCleanupTask периодично освобождава изтекли HOLD-ове
 * (HELD -> AVAILABLE).
 */
@Component
public class HoldCleanupTask {

    private final SeatService seatService;

    public HoldCleanupTask(SeatService seatService) {
        this.seatService = seatService;
    }

    /**
     * На всеки 1 секунда:
     * - сканира местата
     * - освобождава тези, чийто HOLD е изтекъл
     *
     * fixedDelay = чака 1000ms след като предишното изпълнение приключи.
     */
    @Scheduled(fixedDelay = 1000)
    public void cleanupExpiredHolds() {
        seatService.releaseExpiredHolds();
    }
}
