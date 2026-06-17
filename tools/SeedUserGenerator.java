import local.pk154938.helpdesk.util.SecurityUtils;

import java.util.UUID;

/**
 * Pomocniczy program: generuje gotowe instrukcje SQL (INSERT INTO users ...)
 * z poprawnymi hashami PBKDF2, używając tej samej klasy {@link SecurityUtils}
 * co aplikacja. Dzięki temu hash jest liczony z TYM SAMYM pieprzem
 * (SHOP_APP_PEPPER), który jest aktywny w środowisku w momencie uruchomienia.
 *
 * Kompilacja i uruchomienie (z katalogu ProgramowanieObiektoweProjektLab2):
 *   javac --release 11 -encoding UTF-8 -d build \
 *       src/main/java/local/pk154938/helpdesk/util/SecurityUtils.java \
 *       tools/SeedUserGenerator.java
 *   java -cp build SeedUserGenerator > users_seed.sql
 *
 * WAŻNE: jeśli ustawisz SHOP_APP_PEPPER, uruchom ten generator z TĄ SAMĄ
 * wartością zmiennej, inaczej wygenerowane konta nie zalogują się w aplikacji.
 */
public class SeedUserGenerator {

    // Wspólne hasło deweloperskie dla wszystkich kont z tego skryptu.
    private static final String PASSWORD = "Helpdesk1!";

    // username -> rola (kolumna `role` w tabeli users)
    private static final String[][] USERS = {
            {"k.nowak",        "OPERATOR"},
            {"m.lewandowski",  "OPERATOR"},
            {"a.zielinska",    "OPERATOR"},
    };

    public static void main(String[] args) {
        if (!SecurityUtils.isPasswordStrong(PASSWORD)) {
            System.err.println("Hasło nie spełnia wymogów bezpieczeństwa.");
            System.exit(1);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("-- Konta operatorów wygenerowane przez tools/SeedUserGenerator.java\n");
        sb.append("-- Hasło dla wszystkich poniższych kont: ").append(PASSWORD).append('\n');
        sb.append("-- Pieprz użyty do hashowania: ")
          .append(SecurityUtils.isUsingFallbackPepper()
                  ? "WBUDOWANY FALLBACK (SHOP_APP_PEPPER nie ustawiony)"
                  : "z SHOP_APP_PEPPER")
          .append('\n');
        sb.append("INSERT INTO users (id, username, hashed_password, salt, role) VALUES\n");

        for (int i = 0; i < USERS.length; i++) {
            String username = USERS[i][0];
            String role = USERS[i][1];
            String salt = SecurityUtils.generateSalt();
            String hash = SecurityUtils.hashPassword(PASSWORD, salt);
            String id = UUID.randomUUID().toString();
            sb.append("    ('").append(id).append("', '").append(username).append("', '")
              .append(hash).append("', '").append(salt).append("', '").append(role).append("')")
              .append(i < USERS.length - 1 ? ",\n" : "\n");
        }

        sb.append("ON DUPLICATE KEY UPDATE\n");
        sb.append("    hashed_password = VALUES(hashed_password),\n");
        sb.append("    salt            = VALUES(salt),\n");
        sb.append("    role            = VALUES(role);\n");

        System.out.print(sb);
    }
}
