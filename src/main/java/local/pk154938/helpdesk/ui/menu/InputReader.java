package local.pk154938.helpdesk.ui.menu;

/**
 * Console input helpers. An empty input is treated as a user-initiated
 * cancellation — readers that require a value throw {@link CancelledException}
 * so the surrounding flow can short-circuit with a single try/catch.
 */
public final class InputReader {

    private InputReader() {}

    public static class CancelledException extends RuntimeException {
        public CancelledException() { super("Operacja anulowana."); }
    }

    public static String readNonBlankString(String prompt) {
        String s = ConsoleIo.readLine(prompt);
        if (s == null || s.isBlank()) throw new CancelledException();
        return s.trim();
    }

    public static int readPositiveInt(String prompt) {
        String s = readNonBlankString(prompt);
        int v;
        try {
            v = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Niepoprawna liczba całkowita: " + s);
        }
        if (v <= 0)
            throw new IllegalArgumentException("Wartość musi być dodatnia.");
        return v;
    }

    /**
     * Like {@link #readPositiveInt}, but an empty input returns {@code null}
     * instead of cancelling — useful when the field is optional.
     */
    public static Integer readOptionalPositiveInt(String prompt) {
        String s = ConsoleIo.readLine(prompt);
        if (s == null || s.isBlank()) return null;
        int v;
        try {
            v = Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Niepoprawna liczba całkowita: " + s);
        }
        if (v <= 0)
            throw new IllegalArgumentException("Wartość musi być dodatnia.");
        return v;
    }
}
