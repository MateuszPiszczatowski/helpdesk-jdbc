package local.pk154938.shop.domain.trade;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class Delivery extends BaseItemOperation {
    private final UUID orderId;
    private DeliveryStatus status;

    public Delivery(UUID id, Instant timestamp, List<OperationLine> lines,
                    UUID orderId, DeliveryStatus status) {
        super(id, timestamp, lines);
        if (orderId == null)
            throw new IllegalArgumentException("Identyfikator zamówienia nie może być null.");
        if (status == null)
            throw new IllegalArgumentException("Status dostawy nie może być null.");
        this.orderId = orderId;
        this.status = status;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatus status) {
        if (status == null)
            throw new IllegalArgumentException("Status dostawy nie może być null.");
        this.status = status;
    }
}
