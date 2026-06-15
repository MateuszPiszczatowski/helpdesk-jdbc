package local.pk154938.helpdesk.domain.user;

import java.util.Set;
import java.util.UUID;

public class Admin extends User{
    public Admin(String username, String hashedPassword, String salt, Set<Role> roles){
        super(username, hashedPassword, salt, roles);
    }
    Admin(UUID id, String username, String hashedPassword, String salt, Set<Role> roles){
        super(id, username, hashedPassword, salt, roles);
    }

    @Override
    protected User copy(String username, String password) {
        return new Admin(this.getId(), username, password, this.getSalt(), this.getRoles());
    }
}
