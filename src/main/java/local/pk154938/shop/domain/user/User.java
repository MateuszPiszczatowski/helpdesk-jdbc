package local.pk154938.shop.domain.user;

import java.util.Set;

public abstract class User {
    private final String username;
    private final String hashedPassword;
    private final String salt;
    private final Set<Role> roles;

    public User(String username, String hashedPassword, String salt, Set<Role> roles){
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.roles = roles;
        this.salt = salt;
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getSalt() {return salt;}

    public Set<Role> getRoles() {return roles;}
}
