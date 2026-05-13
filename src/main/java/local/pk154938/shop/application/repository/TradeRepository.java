package local.pk154938.shop.application.repository;

import local.pk154938.shop.domain.trade.Delivery;
import local.pk154938.shop.domain.trade.ItemOperation;
import local.pk154938.shop.domain.trade.Order;
import local.pk154938.shop.domain.trade.Return;
import local.pk154938.shop.domain.trade.Sale;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistent store for trade operations. Implementations are expected to
 * persist each operation to durable storage on {@link #save(ItemOperation)}
 * and to rewrite an existing record on {@link #update(ItemOperation)}.
 *
 * <p>Type-specific finders return operations sorted by timestamp, newest
 * first. {@link #findAll()} returns the merged history across all types.
 */
public interface TradeRepository {
    void save(ItemOperation operation);

    /**
     * Updates an already-persisted operation in place. The operation is
     * located by its id within the appropriate type store.
     *
     * @throws java.util.NoSuchElementException if no operation with that id
     *         exists in the corresponding store
     */
    void update(ItemOperation operation);

    List<Order> findOrders();
    List<Delivery> findDeliveries();
    List<Sale> findSales();
    List<Return> findReturns();

    Optional<Order> findOrderById(UUID id);
    Optional<Delivery> findDeliveryById(UUID id);
    Optional<Sale> findSaleById(UUID id);
    Optional<Return> findReturnById(UUID id);

    /**
     * @return all operations of any type, sorted by timestamp newest first
     */
    List<ItemOperation> findAll();
}
