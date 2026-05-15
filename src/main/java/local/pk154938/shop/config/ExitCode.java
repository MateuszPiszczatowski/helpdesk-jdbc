package local.pk154938.shop.config;

public enum ExitCode {
    SUCCESS(0, "Program zakończony pomyślnie."),
    INVALID_CONFIG(1, "Błąd konfiguracji (sprawdź zmienne środowiskowe)."),
    DATABASE_ERROR(2, "Błąd krytyczny bazy danych."),
    SECURITY_ISSUE(3, "Wykryto naruszenie bezpieczeństwa!");

    private final int code;
    private final String message;

    ExitCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}
