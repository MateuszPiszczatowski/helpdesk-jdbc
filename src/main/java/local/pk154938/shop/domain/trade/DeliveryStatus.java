package local.pk154938.shop.domain.trade;

public enum DeliveryStatus {
    DISPATCHED("Nadana"),
    DELIVERED("Dostarczona");

    private final String displayName;

    DeliveryStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
