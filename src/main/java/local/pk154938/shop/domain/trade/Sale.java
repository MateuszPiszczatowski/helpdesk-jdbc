package local.pk154938.shop.domain.trade;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class Sale extends BaseItemOperation {
    public Sale(UUID id, Instant timestamp, List<OperationLine> lines) {
        super(id, timestamp, lines);
    }
}
