package local.pk154938.helpdesk.application.service;

import local.pk154938.helpdesk.application.auth.AuthorizationService;
import local.pk154938.helpdesk.application.auth.Operation;
import local.pk154938.helpdesk.application.repository.TicketRepository;
import local.pk154938.helpdesk.domain.ticket.Ticket;
import local.pk154938.helpdesk.domain.ticket.TicketStatus;
import local.pk154938.helpdesk.domain.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Application service for ticket workflow. Holds all the business rules:
 * authorization, status transitions, and the auto-recalc that flips
 * {@link TicketStatus#IN_PROGRESS} ↔ {@link TicketStatus#DELAYED} whenever
 * the predicted completion time crosses now.
 *
 * <p>Client-facing methods ({@link #openTicket}, {@link #checkTicket},
 * {@link #editClientMessage}) need no logged-in user and identify the
 * client by id + first/last name. Staff-facing methods require an
 * authenticated user with the appropriate {@link Operation}.
 */
public class TicketService {
    private final TicketRepository ticketRepository;
    private final AuthorizationService authService;

    public TicketService(TicketRepository ticketRepository, AuthorizationService authService) {
        this.ticketRepository = ticketRepository;
        this.authService = authService;
    }

    // ---------- client-facing (no auth) ----------

    public Ticket openTicket(String firstName, String lastName, String message) {
        requireNonBlank(firstName, "Imię");
        requireNonBlank(lastName, "Nazwisko");
        requireNonBlank(message, "Treść zgłoszenia");

        Ticket t = new Ticket(
                0, LocalDateTime.now(),
                firstName.trim(), lastName.trim(),
                message.trim(), null,
                null, null, null, null,
                TicketStatus.NOT_STARTED,
                null
        );
        int id = ticketRepository.insert(t);
        t.setId(id);
        return t;
    }

    public Optional<Ticket> checkTicket(int id, String firstName, String lastName) {
        Optional<Ticket> opt = ticketRepository.findForClient(id, firstName, lastName);
        opt.ifPresent(this::refreshStatus);
        return opt;
    }

    public Ticket editClientMessage(int id, String firstName, String lastName, String newMessage) {
        requireNonBlank(newMessage, "Treść zgłoszenia");
        Ticket t = ticketRepository.findForClient(id, firstName, lastName)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono zgłoszenia o podanych danych."));
        refreshStatus(t);
        if (t.getStatus() == TicketStatus.COMPLETED)
            throw new IllegalStateException("Zgłoszenie jest zamknięte — nie można edytować.");

        t.setClientMessage(newMessage.trim());
        t.setClientMessageUpdatedAt(LocalDateTime.now());
        ticketRepository.update(t);
        return t;
    }

    // ---------- staff-facing ----------

    public List<Ticket> listTickets(User currentUser) {
        requireAuthorized(currentUser, Operation.HANDLE_TICKETS);
        List<Ticket> all = ticketRepository.findAll();
        all.forEach(this::refreshStatus);
        return all;
    }

    public Ticket getTicketForStaff(int id, User currentUser) {
        requireAuthorized(currentUser, Operation.HANDLE_TICKETS);
        Ticket t = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono zgłoszenia o ID: " + id));
        refreshStatus(t);
        return t;
    }

    /**
     * Adds or updates the worker message on a ticket. {@code predictedDays} is
     * required when there is no existing predicted completion time; it can
     * be {@code null} on subsequent edits to leave the existing time intact.
     */
    public Ticket addOrEditWorkerMessage(int id, String message, Integer predictedDays, User currentUser) {
        requireAuthorized(currentUser, Operation.HANDLE_TICKETS);
        requireNonBlank(message, "Wiadomość pracownika");
        Ticket t = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono zgłoszenia o ID: " + id));
        refreshStatus(t);
        if (t.getStatus() == TicketStatus.COMPLETED)
            throw new IllegalStateException("Zgłoszenie jest zamknięte — nie można modyfikować.");

        LocalDateTime now = LocalDateTime.now();
        if (t.getPredictedCompletionAt() == null && predictedDays == null)
            throw new IllegalArgumentException(
                    "Pierwsza wiadomość pracownika wymaga podania przewidywanego czasu zakończenia (liczba dni).");
        if (predictedDays != null) {
            if (predictedDays <= 0)
                throw new IllegalArgumentException("Liczba dni musi być dodatnia.");
            t.setPredictedCompletionAt(now.plusDays(predictedDays));
        }

        t.setWorkerMessage(message.trim());
        t.setWorkerMessageAuthor(currentUser.getUsername());
        t.setWorkerMessageUpdatedAt(now);
        t.setStatus(computeStatus(t));
        ticketRepository.update(t);
        return t;
    }

    public Ticket closeTicket(int id, User currentUser) {
        requireAuthorized(currentUser, Operation.HANDLE_TICKETS);
        Ticket t = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono zgłoszenia o ID: " + id));
        if (t.getStatus() == TicketStatus.COMPLETED || t.getClosedAt() != null)
            throw new IllegalStateException("Zgłoszenie jest już zamknięte.");
        t.setClosedAt(LocalDateTime.now());
        t.setStatus(TicketStatus.COMPLETED);
        ticketRepository.update(t);
        return t;
    }

    public void deleteTicket(int id, User currentUser) {
        requireAuthorized(currentUser, Operation.DELETE_TICKET);
        Ticket t = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono zgłoszenia o ID: " + id));
        if (t.getClosedAt() != null)
            throw new IllegalStateException("Nie można usunąć zamkniętego zgłoszenia.");
        ticketRepository.delete(id);
    }

    // ---------- helpers ----------

    private void requireAuthorized(User user, Operation op) {
        if (!authService.isAuthorized(user, op))
            throw new SecurityException("Brak uprawnień do operacji: " + op);
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(fieldName + " nie może być pusty/pusta.");
    }

    /**
     * Recomputes status based on the other fields and persists the change
     * if it differs from the current stored value.
     */
    private void refreshStatus(Ticket t) {
        TicketStatus old = t.getStatus();
        TicketStatus fresh = computeStatus(t);
        if (fresh != old) {
            t.setStatus(fresh);
            ticketRepository.update(t);
        }
    }

    private static TicketStatus computeStatus(Ticket t) {
        if (t.getClosedAt() != null) return TicketStatus.COMPLETED;
        if (t.getWorkerMessage() == null) return TicketStatus.NOT_STARTED;
        if (t.getPredictedCompletionAt() != null
                && t.getPredictedCompletionAt().isBefore(LocalDateTime.now()))
            return TicketStatus.DELAYED;
        return TicketStatus.IN_PROGRESS;
    }
}
