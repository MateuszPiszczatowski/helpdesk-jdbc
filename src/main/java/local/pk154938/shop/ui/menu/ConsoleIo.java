package local.pk154938.shop.ui.menu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Console I/O abstraction. When a real {@link System#console()} exists it is
 * used, so passwords stay masked. Where the console is null (IDE Run/Debug,
 * piped stdin) input falls back to stdin — but only in developer mode
 * ({@code SHOP_APP_DEV_MODE=true}), and then passwords are <em>not</em> masked.
 * Outside developer mode a missing console is treated as a fatal configuration
 * problem surfaced by {@link #isInteractiveInputUnavailable()} rather than a
 * silent, insecure fallback.
 */
public final class ConsoleIo {

    private static final boolean DEV_MODE =
            "true".equalsIgnoreCase(System.getenv("SHOP_APP_DEV_MODE"));

    private static final BufferedReader STDIN =
            new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

    private ConsoleIo() {}

    /**
     * True when there is no real console and the dev-mode stdin fallback is
     * disabled. In that state interactive input is impossible, so the caller
     * should report it and exit instead of letting reads fail later.
     */
    public static boolean isInteractiveInputUnavailable() {
        return System.console() == null && !DEV_MODE;
    }

    /** Mirrors System.console().readLine() — returns null at end of input. */
    public static String readLine() {
        if (System.console() != null) return System.console().readLine();
        requireDevFallback();
        try {
            return STDIN.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException("Błąd odczytu wejścia.", e);
        }
    }

    /** Prints the prompt literally (no printf parsing), then reads a line. */
    public static String readLine(String prompt) {
        System.out.print(prompt);
        System.out.flush();
        return readLine();
    }

    /** Mirrors System.console().readPassword(). Not masked in the fallback path. */
    public static char[] readPassword() {
        if (System.console() != null) return System.console().readPassword();
        requireDevFallback();
        String line = readLine();
        return line == null ? null : line.toCharArray();
    }

    private static void requireDevFallback() {
        if (!DEV_MODE) {
            throw new IllegalStateException(
                    "Aplikacja nie jest uruchomiona w prawidłowej konsoli, " +
                    "a tryb developerski (SHOP_APP_DEV_MODE=true) jest wyłączony.");
        }
    }
}
