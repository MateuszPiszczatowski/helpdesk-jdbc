package local.pk154938.shop.domain.user;

import java.util.Set;
import java.util.UUID;

public class Employee extends User{
    public Employee(String username, String hashedPassword, String salt, Set<Role> roles)
    {
        super(username, hashedPassword, salt, roles);
    }

    protected Employee(UUID id, String username, String hashedPassword, String salt, Set<Role> roles){
        super(id, username, hashedPassword, salt, roles);
    }

    @Override
    protected User copy(String username, String password) {
        return new Employee(this.getId(), username, password, this.getSalt(), this.getRoles());
    }

}
