package local.pk154938.helpdesk.infrastructure.persistence;

import local.pk154938.helpdesk.application.repository.UserRepository;
import local.pk154938.helpdesk.domain.user.Role;
import local.pk154938.helpdesk.domain.user.User;
import local.pk154938.helpdesk.domain.user.UserFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * {@link UserRepository} backed by the MySQL {@code users} table. Identity is
 * the application-generated {@link UUID}, stored as {@code CHAR(36)}. Each user
 * has a single role; the row's {@code role} column selects the concrete
 * subclass on load. {@link #save(User)} upserts by id, so it serves both
 * creation and later password/username changes.
 */
public class JdbcUserRepository implements UserRepository {

    private final Connection connection;

    public JdbcUserRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(User user) {
        String sql = "INSERT INTO users (id, username, hashed_password, salt, role) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE username = VALUES(username), " +
                "hashed_password = VALUES(hashed_password), salt = VALUES(salt), role = VALUES(role)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getId().toString());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getHashedPassword());
            stmt.setString(4, user.getSalt());
            stmt.setString(5, primaryRole(user).name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Błąd zapisu użytkownika: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE LOWER(username) = LOWER(?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(fromRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Błąd odczytu użytkownika: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY username";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<User> result = new ArrayList<>();
            while (rs.next()) result.add(fromRow(rs));
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Błąd odczytu listy użytkowników: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Błąd usuwania użytkownika: " + e.getMessage(), e);
        }
    }

    private static Role primaryRole(User user) {
        return user.getRoles().iterator().next();
    }

    private static User fromRow(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        String username = rs.getString("username");
        String hashedPassword = rs.getString("hashed_password");
        String salt = rs.getString("salt");
        Role role = Role.valueOf(rs.getString("role"));
        return UserFactory.reconstitute(id, username, hashedPassword, salt, role);
    }
}
