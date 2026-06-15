package local.pk154938.shop.domain.user;

import java.util.Set;
import java.util.UUID;

/**
 * Rebuilds {@link User} instances from persisted data, selecting the concrete
 * subclass from the stored {@link Role}. This is the single sanctioned way for
 * persistence adapters to recreate a user with its stored id — the id-bearing
 * constructors of {@link Admin}/{@link Operator} stay package-private.
 */
public final class UserFactory {

    private UserFactory() {}

    public static User reconstitute(UUID id, String username, String hashedPassword,
                                    String salt, Role role) {
        Set<Role> roles = Set.of(role);
        switch (role) {
            case ADMIN:    return new Admin(id, username, hashedPassword, salt, roles);
            case OPERATOR: return new Operator(id, username, hashedPassword, salt, roles);
            default: throw new IllegalArgumentException("Nieobsługiwana rola: " + role);
        }
    }
}
