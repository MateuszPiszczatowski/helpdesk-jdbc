package local.pk154938.shop.application.repository;

import local.pk154938.shop.domain.ticket.Ticket;

import java.util.List;
import java.util.Optional;

public interface TicketRepository {
    /**
     * Inserts a new ticket. The {@code id} of the passed instance is ignored;
     * the DB-generated identifier is returned.
     */
    int insert(Ticket ticket);

    Optional<Ticket> findById(int id);

    /**
     * Returns the ticket only if its first/last name match (case-insensitive)
     * — used for client-side identification via number + name.
     */
    Optional<Ticket> findForClient(int id, String firstName, String lastName);

    /**
     * @return all tickets, newest first (by {@code created_at}).
     */
    List<Ticket> findAll();

    void update(Ticket ticket);

    void delete(int id);
}
