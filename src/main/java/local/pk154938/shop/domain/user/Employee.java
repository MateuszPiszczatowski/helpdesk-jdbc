package local.pk154938.shop.domain.user;

import java.util.Set;

public class Employee extends User{
    public Employee(String username, String hashedPassword, Set<Role> roles)
    {
        super(username, hashedPassword, roles);
    }
}
