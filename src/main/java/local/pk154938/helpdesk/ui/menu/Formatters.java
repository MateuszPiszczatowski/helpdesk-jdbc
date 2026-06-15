package local.pk154938.helpdesk.ui.menu;

import local.pk154938.helpdesk.domain.ticket.Ticket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Formatters {

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Formatters() {}

    public static String time(LocalDateTime t) {
        return t == null ? "—" : TIMESTAMP.format(t);
    }

    public static String renderTicketSummary(Ticket t) {
        return "#" + t.getId()
                + "  |  " + time(t.getCreatedAt())
                + "  |  " + t.getFirstName() + " " + t.getLastName()
                + "  |  " + t.getStatus().displayName();
    }

    public static void printTicketDetail(Ticket t) {
        System.out.println();
        System.out.println("=== Zgłoszenie #" + t.getId() + " ===");
        System.out.println("Utworzono: " + time(t.getCreatedAt()));
        System.out.println("Klient: " + t.getFirstName() + " " + t.getLastName());
        System.out.println("Status: " + t.getStatus().displayName());
        System.out.println();
        System.out.println("Wiadomość klienta"
                + (t.getClientMessageUpdatedAt() != null
                    ? " (edytowana " + time(t.getClientMessageUpdatedAt()) + ")"
                    : "")
                + ":");
        System.out.println("  " + t.getClientMessage());
        if (t.getWorkerMessage() != null) {
            System.out.println();
            System.out.println("Wiadomość pracownika ("
                    + t.getWorkerMessageAuthor()
                    + ", " + time(t.getWorkerMessageUpdatedAt()) + "):");
            System.out.println("  " + t.getWorkerMessage());
            if (t.getPredictedCompletionAt() != null) {
                System.out.println("Przewidywany czas zakończenia: "
                        + time(t.getPredictedCompletionAt()));
            }
        }
        if (t.getClosedAt() != null) {
            System.out.println();
            System.out.println("Zamknięto: " + time(t.getClosedAt()));
        }
    }
}
