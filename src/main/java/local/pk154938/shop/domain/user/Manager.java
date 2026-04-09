package local.pk154938.shop.domain.user;

import java.util.Set;
import java.util.UUID;

public class Manager extends Employee{
    public Manager(String username, String hashedPassword, String salt, Set<Role> roles)
    {
        super(username, hashedPassword, salt, roles);
    }
    private Manager(UUID id, String username, String hashedPassword, String salt, Set<Role> roles){
        super(id, username, hashedPassword, salt, roles);
    }

    @Override
    protected User copy(String username, String password) {
        return new Manager(this.getId(), username, password, this.getSalt(), this.getRoles());
    }
}
