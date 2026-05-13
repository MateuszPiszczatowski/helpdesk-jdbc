package local.pk154938.shop.domain.trade;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Shared base for {@link ItemOperation} implementations: holds id, timestamp
 * and lines. Validates non-null id/timestamp and non-empty lines in the
 * constructor. Concrete subclasses add operation-specific fields.
 */
public abstract class BaseItemOperation implements ItemOperation {
    private final UUID id;
    private final Instant timestamp;
    private final List<OperationLine> lines;

    protected BaseItemOperation(UUID id, Instant timestamp, List<OperationLine> lines) {
        if (id == null)
            throw new IllegalArgumentException("Identyfikator operacji nie może być null.");
        if (timestamp == null)
            throw new IllegalArgumentException("Znacznik czasu nie może być null.");
        if (lines == null || lines.isEmpty())
            throw new IllegalArgumentException("Lista pozycji nie może być pusta.");
        this.id = id;
        this.timestamp = timestamp;
        this.lines = List.copyOf(lines);
    }

    @Override
    public final UUID getId() {
        return id;
    }

    @Override
    public final Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public final List<OperationLine> getLines() {
        return lines;
    }
}
