package local.pk154938.shop.application.auth;

import local.pk154938.shop.domain.user.Permission;
import local.pk154938.shop.domain.user.User;

import java.util.Map;
import java.util.Set;

public class AuthorizationService {
    private final Map<Operation, Set<Permission>> securityMap = Map.ofEntries(
            Map.entry(Operation.ADD_USER, Set.of(Permission.MANAGE_OPERATORS)),
            Map.entry(Operation.MODIFY_USER, Set.of(Permission.MANAGE_OPERATORS)),
            Map.entry(Operation.REMOVE_USER, Set.of(Permission.MANAGE_OPERATORS)),
            Map.entry(Operation.VIEW_USER_LIST, Set.of(Permission.VIEW_USERS))
    );

    public boolean isAuthorized(User user, Operation op) {
        if (op == Operation.ANONYMOUS) return true;
        if (user == null) return false;
        if (op == Operation.AUTHENTICATED) return true;

        Set<Permission> requiredPermissions = securityMap.get(op);
        if (requiredPermissions == null || requiredPermissions.isEmpty())
            return true;

        return requiredPermissions.stream()
                .anyMatch(reqPerm -> user.getRoles().stream().anyMatch(role -> role.hasPermission(reqPerm)));
    }
}
