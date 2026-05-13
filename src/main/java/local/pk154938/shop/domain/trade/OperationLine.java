package local.pk154938.shop.domain.trade;

import java.util.Objects;

public final class OperationLine {
    private final Product product;
    private final int quantity;

    public OperationLine(Product product, int quantity) {
        if (product == null)
            throw new IllegalArgumentException("Produkt nie może być null.");
        if (quantity <= 0)
            throw new IllegalArgumentException("Ilość musi być dodatnia.");
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OperationLine)) return false;
        OperationLine other = (OperationLine) o;
        return quantity == other.quantity && product.equals(other.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product, quantity);
    }
}
