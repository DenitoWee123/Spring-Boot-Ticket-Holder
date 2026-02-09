package ticketholder.model;

/**
 * SeatStatus показва дали дадено място е "свободно", "заето" или "задържано за момента".
 *
 * AVAILABLE - свободно, може да бъде задържано (hold)
 * HELD      - временно задържано за конкретен потребител до изтичане (TTL)
 * SOLD      - финално купено, вече не може да бъде задържано
 */
public enum SeatStatus {
    AVAILABLE,
    HELD,
    SOLD
}
