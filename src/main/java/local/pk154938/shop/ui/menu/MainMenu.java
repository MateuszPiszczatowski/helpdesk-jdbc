package local.pk154938.shop.ui.menu;

import local.pk154938.shop.application.auth.AuthorizationService;
import local.pk154938.shop.application.auth.Operation;
import local.pk154938.shop.application.service.UserService;
import local.pk154938.shop.application.session.Session;
import local.pk154938.shop.domain.user.Role;
import local.pk154938.shop.domain.user.User;
import local.pk154938.shop.util.SecurityUtils;

import java.util.Optional;

public class MainMenu extends BaseMenu {
    private final UserService userService;

    public MainMenu(UserService userService,
                    Session session,
                    AuthorizationService authorizationService) {
        super("Menu główne", session, authorizationService);
        this.userService = userService;
    }

    private void handleLogin() {
        System.out.print("Login: ");
        String login = System.console().readLine();
        System.out.print("Hasło: ");
        String password = new String(System.console().readPassword());

        Optional<User> user = userService.login(login, password);
        if (user.isPresent()) {
            session.login(user.get());
        } else {
            System.out.println("Błędne dane logowania");
        }
    }

    private void logout() {
        session.logout();
        System.out.println("Wylogowano.");
    }

    private void enterUserManagement() {
        new UserManagementMenu(userService, session, authorizationService).show();
    }

    private void changeOwnPassword() {
        System.out.print("Podaj nowe hasło: ");
        String pass = new String(System.console().readPassword());
        if (!SecurityUtils.isPasswordStrong(pass)) {
            System.out.println("BŁĄD: Hasło musi składać się z minimum 8 znaków i zawierać małą literę, wielką literę, cyfrę oraz znak specjalny.");
            return;
        }
        try {
            User updated = userService.changePassword(session.getCurrentUser().getUsername(), pass, session.getCurrentUser());
            session.login(updated);
            System.out.println("Twoje hasło zostało pomyślnie zmienione.");
        } catch (Exception e) {
            System.out.println("BŁĄD: " + e.getMessage());
        }
    }

    @Override
    protected void addOptions() {
        if (session.isLoggedIn()) {
            boolean isAdmin = session.getCurrentUser().getRoles().contains(Role.ADMIN);
            if (isAdmin) {
                addOption("Zarządzanie operatorami", this::enterUserManagement,
                        Operation.VIEW_USER_LIST, Operation.ADD_USER, Operation.REMOVE_USER, Operation.MODIFY_USER);
            } else {
                addOption("Zmień hasło", this::changeOwnPassword, Operation.AUTHENTICATED);
            }
            addOption("Wyloguj", this::logout, Operation.AUTHENTICATED);
        } else {
            addOption("Zaloguj", this::handleLogin, Operation.ANONYMOUS);
        }
    }
}
