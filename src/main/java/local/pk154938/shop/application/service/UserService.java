package local.pk154938.shop.application.service;

import local.pk154938.shop.application.auth.AuthorizationService;
import local.pk154938.shop.application.auth.Operation;
import local.pk154938.shop.application.repository.UserRepository;
import local.pk154938.shop.domain.user.Operator;
import local.pk154938.shop.domain.user.Role;
import local.pk154938.shop.domain.user.User;
import local.pk154938.shop.util.SecurityUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class UserService {
    private final UserRepository userRepository;
    private final AuthorizationService authService;

    public UserService(UserRepository userRepository, AuthorizationService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    public Optional<User> login(String username, String rawPassword) {
        return userRepository.findByUsername(username).filter(user -> {
            String attemptHash = SecurityUtils.hashPassword(rawPassword, user.getSalt());
            return attemptHash.equals(user.getHashedPassword());
        });
    }

    public void createAndAddUser(String username, String password, User currentUser) {
        if (!authService.isAuthorized(currentUser, Operation.ADD_USER))
            throw new SecurityException("Brak uprawnień do tworzenia operatora.");
        if (userRepository.findByUsername(username).isPresent())
            throw new IllegalStateException("Użytkownik o podanym loginie już istnieje.");

        String salt = SecurityUtils.generateSalt();
        String hashedPassword = SecurityUtils.hashPassword(password, salt);
        Operator newOperator = new Operator(username, hashedPassword, salt, Set.of(Role.OPERATOR));
        userRepository.save(newOperator);
    }

    public void removeUser(String targetUsername, User currentUser) {
        User targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika o podanym loginie."));
        if (targetUser.getRoles().contains(Role.ADMIN))
            throw new IllegalStateException("Nie można usunąć administratora.");
        if (!authService.isAuthorized(currentUser, Operation.REMOVE_USER))
            throw new SecurityException("Brak uprawnień do usunięcia użytkownika.");

        userRepository.delete(targetUser.getId());
    }

    public User changePassword(String targetUsername, String newRawPassword, User currentUser) {
        User targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika o podanym loginie."));

        boolean isSelf = targetUser.getId().equals(currentUser.getId());
        if (!isSelf) {
            if (targetUser.getRoles().contains(Role.ADMIN))
                throw new IllegalStateException("Nie można zmienić hasła administratora.");
            if (!authService.isAuthorized(currentUser, Operation.MODIFY_USER))
                throw new SecurityException("Brak uprawnień do modyfikacji użytkownika.");
        }

        String newHashedPassword = SecurityUtils.hashPassword(newRawPassword, targetUser.getSalt());
        User updatedUser = targetUser.withPassword(newHashedPassword);
        userRepository.save(updatedUser);
        return updatedUser;
    }

    public User changeUsername(String oldUsername, String newUsername, User currentUser) {
        if (oldUsername.equalsIgnoreCase(newUsername))
            return currentUser;
        User targetUser = userRepository.findByUsername(oldUsername)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika o podanym loginie."));
        if (targetUser.getRoles().contains(Role.ADMIN))
            throw new IllegalStateException("Nie można zmienić loginu administratora.");
        if (userRepository.findByUsername(newUsername).isPresent())
            throw new IllegalStateException("Użytkownik o podanym loginie już istnieje.");
        if (!authService.isAuthorized(currentUser, Operation.MODIFY_USER))
            throw new SecurityException("Brak uprawnień do modyfikacji użytkownika.");

        User updatedUser = targetUser.withUsername(newUsername);
        userRepository.save(updatedUser);
        return updatedUser;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
