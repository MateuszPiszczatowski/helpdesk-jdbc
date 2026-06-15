package local.pk154938.shop.ui.menu;

import local.pk154938.shop.application.auth.AuthorizationService;
import local.pk154938.shop.application.auth.Operation;
import local.pk154938.shop.application.service.TicketService;
import local.pk154938.shop.application.service.UserService;
import local.pk154938.shop.application.session.Session;
import local.pk154938.shop.domain.ticket.Ticket;
import local.pk154938.shop.domain.ticket.TicketStatus;
import local.pk154938.shop.domain.user.Role;
import local.pk154938.shop.domain.user.User;
import local.pk154938.shop.util.SecurityUtils;

import java.util.Optional;

public class MainMenu extends BaseMenu {
    private final UserService userService;
    private final TicketService ticketService;

    public MainMenu(UserService userService,
                    TicketService ticketService,
                    Session session,
                    AuthorizationService authorizationService) {
        super("Menu główne", session, authorizationService);
        this.userService = userService;
        this.ticketService = ticketService;
    }

    @Override
    protected void addOptions() {
        if (session.isLoggedIn()) {
            boolean isAdmin = session.getCurrentUser().getRoles().contains(Role.ADMIN);
            if (isAdmin) {
                addOption("Zarządzanie operatorami", this::enterUserManagement,
                        Operation.VIEW_USER_LIST, Operation.ADD_USER,
                        Operation.REMOVE_USER, Operation.MODIFY_USER);
            }
            addOption("Zmień hasło", this::changeOwnPassword, Operation.AUTHENTICATED);
            addOption("Obsługa zgłoszeń", this::enterTicketManagement,
                    Operation.HANDLE_TICKETS, Operation.DELETE_TICKET);
            addOption("Wyloguj", this::logout, Operation.AUTHENTICATED);
        } else {
            addOption("Otwórz zgłoszenie", this::openClientTicket, Operation.ANONYMOUS);
            addOption("Sprawdź zgłoszenie", this::checkClientTicket, Operation.ANONYMOUS);
            addOption("Edytuj zgłoszenie", this::editClientTicket, Operation.ANONYMOUS);
            addOption("Zaloguj", this::handleLogin, Operation.ANONYMOUS);
        }
    }

    // ---------- staff flows ----------

    private void handleLogin() {
        System.out.print("Login: ");
        String login = ConsoleIo.readLine();
        System.out.print("Hasło: ");
        String password = new String(ConsoleIo.readPassword());

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

    private void enterTicketManagement() {
        new TicketManagementMenu(ticketService, session, authorizationService).show();
    }

    private void changeOwnPassword() {
        System.out.print("Podaj nowe hasło: ");
        String pass = new String(ConsoleIo.readPassword());
        if (!SecurityUtils.isPasswordStrong(pass)) {
            System.out.println("BŁĄD: Hasło musi składać się z minimum 8 znaków i zawierać małą literę, wielką literę, cyfrę oraz znak specjalny.");
            return;
        }
        try {
            User updated = userService.changePassword(
                    session.getCurrentUser().getUsername(), pass, session.getCurrentUser());
            session.login(updated);
            System.out.println("Twoje hasło zostało pomyślnie zmienione.");
        } catch (Exception e) {
            System.out.println("BŁĄD: " + e.getMessage());
        }
    }

    // ---------- client flows ----------

    private void openClientTicket() {
        try {
            String firstName = InputReader.readNonBlankString("Imię: ");
            String lastName = InputReader.readNonBlankString("Nazwisko: ");
            String message = InputReader.readNonBlankString("Treść zgłoszenia: ");
            Ticket t = ticketService.openTicket(firstName, lastName, message);
            System.out.println();
            System.out.println("Zgłoszenie przyjęte. Twój numer zgłoszenia: " + t.getId());
            System.out.println("Zapisz go — będzie potrzebny do sprawdzania statusu zgłoszenia.");
        } catch (InputReader.CancelledException e) {
            System.out.println("Anulowano.");
        } catch (Exception e) {
            System.out.println("BŁĄD: " + e.getMessage());
        }
    }

    private void checkClientTicket() {
        try {
            int id = InputReader.readPositiveInt("Numer zgłoszenia: ");
            String firstName = InputReader.readNonBlankString("Imię: ");
            String lastName = InputReader.readNonBlankString("Nazwisko: ");
            Optional<Ticket> opt = ticketService.checkTicket(id, firstName, lastName);
            if (opt.isEmpty()) {
                System.out.println("Nie znaleziono zgłoszenia o podanych danych.");
                return;
            }
            Ticket t = opt.get();
            if (t.getStatus() == TicketStatus.COMPLETED) {
                System.out.println("Zgłoszenie zamknięte z dnia "
                        + Formatters.time(t.getClosedAt()) + ".");
                return;
            }
            Formatters.printTicketDetail(t);
        } catch (InputReader.CancelledException e) {
            System.out.println("Anulowano.");
        } catch (Exception e) {
            System.out.println("BŁĄD: " + e.getMessage());
        }
    }

    private void editClientTicket() {
        try {
            int id = InputReader.readPositiveInt("Numer zgłoszenia: ");
            String firstName = InputReader.readNonBlankString("Imię: ");
            String lastName = InputReader.readNonBlankString("Nazwisko: ");
            String newMessage = InputReader.readNonBlankString("Nowa treść zgłoszenia: ");
            ticketService.editClientMessage(id, firstName, lastName, newMessage);
            System.out.println("Zaktualizowano zgłoszenie.");
        } catch (InputReader.CancelledException e) {
            System.out.println("Anulowano.");
        } catch (Exception e) {
            System.out.println("BŁĄD: " + e.getMessage());
        }
    }
}
