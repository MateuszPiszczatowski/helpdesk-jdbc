package local.pk154938.helpdesk.domain.ticket;

public enum TicketStatus {
    NOT_STARTED("Nierozpoczęte"),
    IN_PROGRESS("Rozpoczęte"),
    DELAYED("Opóźnione"),
    COMPLETED("Zakończone");

    private final String displayName;

    TicketStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
