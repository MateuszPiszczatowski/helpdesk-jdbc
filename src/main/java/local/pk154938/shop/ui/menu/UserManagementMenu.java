package local.pk154938.shop.ui.menu;

import local.pk154938.shop.application.auth.AuthorizationService;
import local.pk154938.shop.application.auth.Operation;
import local.pk154938.shop.application.service.UserService;
import local.pk154938.shop.application.session.Session;
import local.pk154938.shop.util.SecurityUtils;


public class UserManagementMenu extends BaseMenu {
    private final UserService userService;
    private final Session session;

    public UserManagementMenu(UserService userService, Session session, AuthorizationService authorizationService) {
        super("ZARZĄDZANIE OPERATORAMI", session, authorizationService);
        this.userService = userService;
        this.session = session;
    }

    @Override
    protected void addOptions() {
        addOption("Lista operatorów", this::list, Operation.VIEW_USER_LIST);
        addOption("Dodaj operatora", this::add, Operation.ADD_USER);
        addOption("Usuń operatora", this::remove, Operation.REMOVE_USER);
        addOption("Modyfikuj operatora", this::changeOtherUser, Operation.MODIFY_USER);
    }

    private static final String SELF_OPERATION_BLOCKED =
            "Administrator nie może modyfikować własnego konta.";

    private void list() {
        System.out.println("\n--- LISTA UŻYTKOWNIKÓW ---");
        userService.getAllUsers().forEach(u ->
                System.out.println("- " + u.getUsername() + " (" + u.getClass().getSimpleName() + ")"));
    }

    private void add() {
        System.out.print("Podaj login: ");
        String login = System.console().readLine();
        if (login.isBlank()) {
            System.out.println("Dodawanie anulowane.");
            return;
        }
        System.out.print("Podaj hasło: ");
        String pass = new String(System.console().readPassword());

        if (!SecurityUtils.isPasswordStrong(pass)) {
            System.out.println("BŁĄD: Hasło musi składać się z minimum 8 znaków i zawierać małą literę, wielką literę, cyfrę oraz znak specjalny.");
            return;
        }

        try {
            userService.createAndAddUser(login, pass, session.getCurrentUser());
            System.out.println("Dodano operatora.");
        } catch (SecurityException e) {
            System.out.println("ODMOWA DOSTĘPU: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("BŁĄD: " + e.getMessage());
        }
    }

    private void remove() {
        System.out.print("Podaj login operatora do usunięcia: ");
        String login = System.console().readLine();
        if (login.isBlank()) {
            System.out.println("Usuwanie anulowane.");
            return;
        }
        if (login.equalsIgnoreCase(session.getCurrentUser().getUsername())) {
            System.out.println(SELF_OPERATION_BLOCKED);
            return;
        }

        try {
            userService.removeUser(login, session.getCurrentUser());
            System.out.println("Pomyślnie usunięto użytkownika: " + login);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("BŁĄD: " + e.getMessage());
        } catch (SecurityException e) {
            System.out.println("ODMOWA DOSTĘPU: " + e.getMessage());
        }
    }

    private void changeOtherUser() {
        System.out.print("Podaj login operatora do modyfikacji: ");
        String login = System.console().readLine();
        if (login.isBlank()) {
            System.out.println("Anulowano.");
            return;
        }

        if (login.equalsIgnoreCase(session.getCurrentUser().getUsername())) {
            System.out.println(SELF_OPERATION_BLOCKED);
            return;
        }

        System.out.println("Wybierz co chcesz zmienić: 1. Nazwa użytkownika | 2. Hasło");
        System.out.print("Wybór: ");
        String choice = System.console().readLine();

        try {
            if ("1".equals(choice)) {
                System.out.print("Podaj nową nazwę użytkownika: ");
                String newLogin = System.console().readLine();
                if (newLogin.isBlank()) return;
                userService.changeUsername(login, newLogin, session.getCurrentUser());
                System.out.println("Pomyślnie zmieniono nazwę użytkownika.");
            } else if ("2".equals(choice)) {
                System.out.print("Podaj nowe hasło: ");
                String pass = new String(System.console().readPassword());
                if (!SecurityUtils.isPasswordStrong(pass)) {
                    System.out.println("BŁĄD: Hasło nie spełnia wymogów bezpieczeństwa.");
                    return;
                }
                userService.changePassword(login, pass, session.getCurrentUser());
                System.out.println("Pomyślnie zmieniono hasło.");
            } else {
                System.out.println("Niepoprawny wybór.");
            }
        } catch (SecurityException e) {
            System.out.println("ODMOWA DOSTĘPU: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("BŁĄD: " + e.getMessage());
        }
    }
}
