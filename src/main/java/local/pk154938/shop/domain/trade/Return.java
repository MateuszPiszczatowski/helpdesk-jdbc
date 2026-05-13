package local.pk154938.shop.domain.trade;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A return of goods to a supplier. Always references the originating {@link Order}.
 * (The class name shadows no keyword — {@code return} is a reserved word in Java
 * but {@code Return} as a type identifier is legal and parses correctly.)
 */
public final class Return extends BaseItemOperation {
    private final UUID orderId;

    public Return(UUID id, Instant timestamp, List<OperationLine> lines, UUID orderId) {
        super(id, timestamp, lines);
        if (orderId == null)
            throw new IllegalArgumentException("Identyfikator zamówienia nie może być null.");
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }
}
