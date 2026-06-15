package local.pk154938.helpdesk.config;

import local.pk154938.helpdesk.application.auth.AuthorizationService;
import local.pk154938.helpdesk.application.repository.TicketRepository;
import local.pk154938.helpdesk.application.repository.UserRepository;
import local.pk154938.helpdesk.application.service.TicketService;
import local.pk154938.helpdesk.application.service.UserService;
import local.pk154938.helpdesk.application.session.Session;
import local.pk154938.helpdesk.infrastructure.persistence.DbConnection;
import local.pk154938.helpdesk.infrastructure.persistence.JdbcUserRepository;
import local.pk154938.helpdesk.infrastructure.persistence.JdbcTicketRepository;
import local.pk154938.helpdesk.ui.menu.ConsoleIo;
import local.pk154938.helpdesk.ui.menu.MainMenu;
import local.pk154938.helpdesk.util.SecurityUtils;

import java.sql.SQLException;

public class Main {
    /**
     * Prints the exit message, waits for the user to press Enter (so a
     * terminal that auto-closes still gives a chance to read the cause),
     * and terminates the JVM with the given exit code.
     */
    public static void terminate(ExitCode exitCode) {
        System.err.println("ZAMYKANIE: " + exitCode.getMessage());
        if (System.console() != null) {
            System.console().readLine("Naciśnij Enter, aby zamknąć: ");
        }
        System.exit(exitCode.getCode());
    }

    /**
     * Warns when the app is hashing with the insecure built-in pepper because
     * {@code SHOP_APP_PEPPER} was not set. Not fatal — the app still runs — but
     * the operator must know the security implications.
     */
    private static void warnIfInsecurePepper() {
        if (!SecurityUtils.isUsingFallbackPepper()) {
            return;
        }
        System.err.println();
        System.err.println("########################################################");
        System.err.println("# UWAGA: zmienna SHOP_APP_PEPPER nie jest ustawiona.   #");
        System.err.println("# Hasła są hashowane wbudowanym, NIEBEZPIECZNYM         #");
        System.err.println("# pieprzem. Ustaw SHOP_APP_PEPPER przed użyciem         #");
        System.err.println("# produkcyjnym. Konta utworzone teraz przestaną się    #");
        System.err.println("# logować po późniejszym ustawieniu prawdziwego pieprzu.#");
        System.err.println("########################################################");
        System.err.println();
    }

    public static void main(String[] args) {
        Session session = new Session();

        warnIfInsecurePepper();

        if (ConsoleIo.isInteractiveInputUnavailable()) {
            System.err.println("Aplikacja nie jest uruchomiona w prawidłowej konsoli "
                    + "(brak System.console()).");
            System.err.println("Uruchom ją z terminala, albo ustaw SHOP_APP_DEV_MODE=true, "
                    + "aby włączyć tryb developerski z odczytem ze standardowego wejścia.");
            terminate(ExitCode.INVALID_CONFIG);
            return;
        }

        DbConnection db = null;
        try {
            db = new DbConnection();
            db.initSchema();
        } catch (SQLException e) {
            System.out.println("Nie udało się połączyć z bazą danych: " + e.getMessage());
            terminate(ExitCode.DATABASE_ERROR);
            return;
        }

        try {
            UserRepository userRepository = new JdbcUserRepository(db.getConnection());

            DataSeeder dataSeeder = new DataSeeder(userRepository);
            try {
                dataSeeder.seedAdminIfMissing();
            } catch (IllegalStateException e) {
                System.out.println(e.getMessage());
                terminate(ExitCode.INVALID_CONFIG);
                return;
            }

            AuthorizationService authService = new AuthorizationService();
            UserService userService = new UserService(userRepository, authService);
            TicketRepository ticketRepository = new JdbcTicketRepository(db.getConnection());
            TicketService ticketService = new TicketService(ticketRepository, authService);

            MainMenu menu = new MainMenu(userService, ticketService, session, authService);
            menu.show();
        } finally {
            db.close();
        }
    }
}
