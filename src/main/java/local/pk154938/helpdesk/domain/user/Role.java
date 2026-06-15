package local.pk154938.helpdesk.domain.user;

import java.util.Set;

public enum Role {
    ADMIN(Set.of(Permission.values())),
    OPERATOR(Set.of(Permission.HANDLE_TICKETS));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions){
        this.permissions = permissions;
    }

    public boolean hasPermission(Permission p){
        return permissions.contains(p);
    }
}
