package local.pk154938.helpdesk.ui.menu;

import local.pk154938.helpdesk.application.auth.AuthorizationService;
import local.pk154938.helpdesk.application.auth.Operation;
import local.pk154938.helpdesk.application.service.TicketService;
import local.pk154938.helpdesk.application.session.Session;
import local.pk154938.helpdesk.domain.ticket.Ticket;
import local.pk154938.helpdesk.domain.ticket.TicketStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TicketManagementMenu extends BaseMenu {
    private final TicketService ticketService;

    public TicketManagementMenu(TicketService ticketService,
                                Session session,
                                AuthorizationService authorizationService) {
        super("OBSŁUGA ZGŁOSZEŃ", session, authorizationService);
        this.ticketService = ticketService;
    }

    @Override
    protected void addOptions() {
        addOption("Lista zgłoszeń", this::listTickets, Operation.HANDLE_TICKETS);
        addOption("Szczegóły zgłoszenia", this::viewDetails, Operation.HANDLE_TICKETS);
        addOption("Dodaj / edytuj wiadomość", this::addOrEditMessage, Operation.HANDLE_TICKETS);
        addOption("Zamknij zgłoszenie", this::closeTicket, Operation.HANDLE_TICKETS);
        addOption("Usuń zgłoszenie", this::deleteTicket, Operation.DELETE_TICKET);
    }

    private void listTickets() {
        try {
            List<Ticket> all = ticketService.listTickets(session.getCurrentUser());
            PaginatedSelector.display("Lista zgłoszeń", all, Formatters::renderTicketSummary);
        } catch (SecurityException e) {
            System.out.println("ODMOWA DOSTĘPU: " + e.getMessage());
        }
    }

    private void viewDetails() {
        try {
            int id = InputReader.readPositiveInt("Numer zgłoszenia: ");
            Ticket t = ticketService.getTicketForStaff(id, session.getCurrentUser());
            Formatters.printTicketDetail(t);
        } catch (InputReader.CancelledException e) {
            System.out.println("Anulowano.");
        } catch (SecurityException e) {
            System.out.println("ODMOWA DOSTĘPU: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("BŁĄD: " + e.getMessage());
        }
    }

    private void addOrEditMessage() {
        Optional<Ticket> selected = pickOpenTicket("Wybierz zgłoszenie do edycji wiadomości");
        if (selected.isEmpty()) {
            System.out.println("Anulowano.");
            return;
        }
        Ticket t = selected.get();
        try {
            if (t.getWorkerMessage() != null) {
                System.out.println("Aktualna wiadomość: " + t.getWorkerMessage());
                System.out.println("Aktualny przewidywany czas: "
                        + Formatters.time(t.getPredictedCompletionAt()));
            }
            String message = InputReader.readNonBlankString("Nowa wiadomość: ");
            Integer predictedDays;
            if (t.getPredictedCompletionAt() == null) {
                predictedDays = InputReader.readPositiveInt(
                        "Przewidywany czas zakończenia (liczba dni od dziś): ");
            } else {
                predictedDays = InputReader.readOptionalPositiveInt(
                        "Nowy przewidywany czas (liczba dni, puste = bez zmian): ");
            }
            ticketService.addOrEditWorkerMessage(
                    t.getId(), message, predictedDays, session.getCurrentUser());
            System.out.println("Zapisano wiadomość.");
        } catch (InputReader.CancelledException e) {
            System.out.println("Anulowano.");
        } catch (SecurityException e) {
            System.out.println("ODMOWA DOSTĘPU: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("BŁĄD: " + e.getMessage());
        }
    }

    private void closeTicket() {
        Optional<Ticket> selected = pickOpenTicket("Wybierz zgłoszenie do zamknięcia");
        if (selected.isEmpty()) {
            System.out.println("Anulowano.");
            return;
        }
        try {
            ticketService.closeTicket(selected.get().getId(), session.getCurrentUser());
            System.out.println("Zgłoszenie zamknięte.");
        } catch (SecurityException e) {
            System.out.println("ODMOWA DOSTĘPU: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("BŁĄD: " + e.getMessage());
        }
    }

    private void deleteTicket() {
        Optional<Ticket> selected = pickOpenTicket("Wybierz zgłoszenie do usunięcia");
        if (selected.isEmpty()) {
            System.out.println("Anulowano.");
            return;
        }
        try {
            String confirm = InputReader.readNonBlankString(
                    "Czy na pewno usunąć zgłoszenie #" + selected.get().getId() + "? (T/N): ");
            if (!confirm.equalsIgnoreCase("T")) {
                System.out.println("Anulowano.");
                return;
            }
            ticketService.deleteTicket(selected.get().getId(), session.getCurrentUser());
            System.out.println("Zgłoszenie usunięte.");
        } catch (InputReader.CancelledException e) {
            System.out.println("Anulowano.");
        } catch (SecurityException e) {
            System.out.println("ODMOWA DOSTĘPU: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("BŁĄD: " + e.getMessage());
        }
    }

    private Optional<Ticket> pickOpenTicket(String header) {
        List<Ticket> open;
        try {
            open = ticketService.listTickets(session.getCurrentUser()).stream()
                    .filter(t -> t.getStatus() != TicketStatus.COMPLETED)
                    .collect(Collectors.toList());
        } catch (SecurityException e) {
            System.out.println("ODMOWA DOSTĘPU: " + e.getMessage());
            return Optional.empty();
        }
        return PaginatedSelector.selectOne(header, open, Formatters::renderTicketSummary);
    }
}
