package local.pk154938.shop.domain.trade;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Common contract for all trade operations: an immutable identity, an
 * immutable timestamp, and a list of product lines.
 *
 * <p>Concrete implementations may carry additional fields (counterparty,
 * status, references to other operations) and some of those fields may be
 * mutable — see individual subclasses.
 */
public interface ItemOperation {
    UUID getId();
    Instant getTimestamp();
    List<OperationLine> getLines();
}
