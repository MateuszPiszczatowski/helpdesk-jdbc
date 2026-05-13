package local.pk154938.shop.domain.trade;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class Order extends BaseItemOperation {
    private final String supplierName;
    private OrderStatus status;

    public Order(UUID id, Instant timestamp, List<OperationLine> lines,
                 String supplierName, OrderStatus status) {
        super(id, timestamp, lines);
        if (supplierName == null || supplierName.isBlank())
            throw new IllegalArgumentException("Nazwa dostawcy nie może być pusta.");
        if (status == null)
            throw new IllegalArgumentException("Status zamówienia nie może być null.");
        this.supplierName = supplierName.trim();
        this.status = status;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        if (status == null)
            throw new IllegalArgumentException("Status zamówienia nie może być null.");
        this.status = status;
    }
}
