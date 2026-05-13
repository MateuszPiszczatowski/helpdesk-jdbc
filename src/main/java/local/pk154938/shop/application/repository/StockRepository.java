package local.pk154938.shop.application.repository;

import local.pk154938.shop.domain.trade.Product;

import java.util.Map;

public interface StockRepository {
    /**
     * Adds the given amount to the stock of the product.
     * @throws IllegalArgumentException if {@code amount} is not positive
     */
    void increase(Product product, int amount);

    /**
     * Subtracts the given amount from the stock of the product.
     * @throws IllegalArgumentException if {@code amount} is not positive
     * @throws IllegalStateException if the resulting stock would be negative
     */
    void decrease(Product product, int amount);

    /**
     * @return current stock for the given product, or 0 if the product has
     *         never been stocked
     */
    int getQuantity(Product product);

    /**
     * @return a snapshot map of all known products with quantity &gt; 0
     */
    Map<Product, Integer> getAll();
}
