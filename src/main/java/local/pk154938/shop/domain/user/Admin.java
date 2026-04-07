package local.pk154938.shop.domain.user;

import java.util.Set;

public class Admin extends User{
    public Admin(String username, String hashedPassword, String salt, Set<Role> roles){
        super(username, hashedPassword, salt, roles);
    }
}
