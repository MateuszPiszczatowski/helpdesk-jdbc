package local.pk154938.shop.domain.user;

import java.util.Set;
import java.util.UUID;

public class Operator extends User {
    public Operator(String username, String hashedPassword, String salt, Set<Role> roles) {
        super(username, hashedPassword, salt, roles);
    }

    protected Operator(UUID id, String username, String hashedPassword, String salt, Set<Role> roles) {
        super(id, username, hashedPassword, salt, roles);
    }

    @Override
    protected User copy(String username, String password) {
        return new Operator(this.getId(), username, password, this.getSalt(), this.getRoles());
    }
}
