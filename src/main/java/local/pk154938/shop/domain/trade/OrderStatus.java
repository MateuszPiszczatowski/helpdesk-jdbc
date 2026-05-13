package local.pk154938.shop.domain.trade;

public enum OrderStatus {
    OPEN("Otwarte"),
    PARTIAL("Częściowo dostarczone"),
    FULFILLED("Zrealizowane"),
    CANCELLED("Anulowane");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
