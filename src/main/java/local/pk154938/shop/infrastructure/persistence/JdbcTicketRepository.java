package local.pk154938.shop.infrastructure.persistence;

import local.pk154938.shop.application.repository.TicketRepository;
import local.pk154938.shop.domain.ticket.Ticket;
import local.pk154938.shop.domain.ticket.TicketStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTicketRepository implements TicketRepository {

    private final Connection connection;

    public JdbcTicketRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int insert(Ticket ticket) {
        String sql = "INSERT INTO tickets (" +
                "created_at, first_name, last_name, " +
                "client_message, client_message_updated_at, " +
                "worker_message, worker_message_author, worker_message_updated_at, " +
                "predicted_completion_at, status, closed_at" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setObject(1, ticket.getCreatedAt());
            stmt.setString(2, ticket.getFirstName());
            stmt.setString(3, ticket.getLastName());
            stmt.setString(4, ticket.getClientMessage());
            stmt.setObject(5, ticket.getClientMessageUpdatedAt());
            stmt.setString(6, ticket.getWorkerMessage());
            stmt.setString(7, ticket.getWorkerMessageAuthor());
            stmt.setObject(8, ticket.getWorkerMessageUpdatedAt());
            stmt.setObject(9, ticket.getPredictedCompletionAt());
            stmt.setString(10, ticket.getStatus().name());
            stmt.setObject(11, ticket.getClosedAt());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("Nie zwrócono wygenerowanego ID zgłoszenia.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Błąd zapisu zgłoszenia: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Ticket> findById(int id) {
        String sql = "SELECT * FROM tickets WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(fromRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Błąd odczytu zgłoszenia: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Ticket> findForClient(int id, String firstName, String lastName) {
        String sql = "SELECT * FROM tickets WHERE id = ? " +
                "AND LOWER(first_name) = LOWER(?) AND LOWER(last_name) = LOWER(?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(fromRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Błąd odczytu zgłoszenia: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Ticket> findAll() {
        String sql = "SELECT * FROM tickets ORDER BY created_at DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<Ticket> result = new ArrayList<>();
            while (rs.next()) result.add(fromRow(rs));
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Błąd odczytu listy zgłoszeń: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Ticket ticket) {
        String sql = "UPDATE tickets SET " +
                "client_message = ?, client_message_updated_at = ?, " +
                "worker_message = ?, worker_message_author = ?, worker_message_updated_at = ?, " +
                "predicted_completion_at = ?, status = ?, closed_at = ? " +
                "WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ticket.getClientMessage());
            stmt.setObject(2, ticket.getClientMessageUpdatedAt());
            stmt.setString(3, ticket.getWorkerMessage());
            stmt.setString(4, ticket.getWorkerMessageAuthor());
            stmt.setObject(5, ticket.getWorkerMessageUpdatedAt());
            stmt.setObject(6, ticket.getPredictedCompletionAt());
            stmt.setString(7, ticket.getStatus().name());
            stmt.setObject(8, ticket.getClosedAt());
            stmt.setInt(9, ticket.getId());
            int rows = stmt.executeUpdate();
            if (rows == 0)
                throw new SQLException("Brak zgłoszenia o ID " + ticket.getId() + " do aktualizacji.");
        } catch (SQLException e) {
            throw new IllegalStateException("Błąd aktualizacji zgłoszenia: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM tickets WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            if (rows == 0)
                throw new SQLException("Brak zgłoszenia o ID " + id + " do usunięcia.");
        } catch (SQLException e) {
            throw new IllegalStateException("Błąd usuwania zgłoszenia: " + e.getMessage(), e);
        }
    }

    private static Ticket fromRow(ResultSet rs) throws SQLException {
        return new Ticket(
                rs.getInt("id"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("client_message"),
                rs.getObject("client_message_updated_at", LocalDateTime.class),
                rs.getString("worker_message"),
                rs.getString("worker_message_author"),
                rs.getObject("worker_message_updated_at", LocalDateTime.class),
                rs.getObject("predicted_completion_at", LocalDateTime.class),
                TicketStatus.valueOf(rs.getString("status")),
                rs.getObject("closed_at", LocalDateTime.class)
        );
    }
}
