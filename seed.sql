-- ---------------------------------------------------------------------------
-- Dane testowe (seed) dla Lab2 — spójna historia zgłoszeń helpdesku
-- oraz konta operatorów obsługujących te zgłoszenia.
--
-- Wypełnia tabelę `tickets` przykładową historią obejmującą wszystkie statusy:
--   NOT_STARTED  — zgłoszenie przyjęte, brak odpowiedzi pracownika,
--   IN_PROGRESS  — pracownik odpowiedział, termin realizacji w przyszłości,
--   DELAYED      — pracownik odpowiedział, termin realizacji już minął,
--   COMPLETED    — zgłoszenie zamknięte (closed_at ustawione).
-- oraz tabelę `users` o kontami operatorów (autorzy odpowiedzi).
--
-- Statusy są ZGODNE z logiką TicketService.computeStatus():
--   closed_at != NULL                 -> COMPLETED
--   worker_message == NULL            -> NOT_STARTED
--   predicted_completion_at < teraz   -> DELAYED
--   w przeciwnym razie                -> IN_PROGRESS
-- Stan "teraz" przyjęto na 2026-06-16. Uwaga: zgłoszenia IN_PROGRESS mają
-- przewidywany termin w przyszłości względem tej daty — jeśli uruchomisz
-- aplikację znacznie później, aplikacja sama przeliczy je na DELAYED (to
-- prawidłowe, zamierzone zachowanie funkcji terminów).
--
-- !!! PIEPRZ (SHOP_APP_PEPPER) !!!
-- Hashe haseł operatorów poniżej wygenerowano programem
-- tools/SeedUserGenerator.java z WBUDOWANYM pieprzem fallback (tj. przy
-- NIEUSTAWIONEJ zmiennej SHOP_APP_PEPPER) — tak samo, jak domyślnie tworzone
-- jest konto 'admin'. Jeśli uruchamiasz aplikację z ustawionym SHOP_APP_PEPPER,
-- te konta SIĘ NIE ZALOGUJĄ — wygeneruj wtedy nowe wiersze tym samym
-- generatorem przy ustawionej zmiennej i podmień blok INSERT INTO users.
--
-- Uruchomienie:
--   mysql -u root -p < seed.sql
-- lub w kliencie GUI: otwórz i wykonaj cały plik.
-- ---------------------------------------------------------------------------

SET NAMES utf8mb4;

-- Baza i tabele tworzą się też automatycznie przy starcie aplikacji
-- (DbConnection.initSchema). Tworzymy je tu dla samodzielnego uruchomienia.
CREATE DATABASE IF NOT EXISTS tickets
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tickets;

CREATE TABLE IF NOT EXISTS tickets (
    id                          INT AUTO_INCREMENT PRIMARY KEY,
    created_at                  DATETIME     NOT NULL,
    first_name                  VARCHAR(100) NOT NULL,
    last_name                   VARCHAR(100) NOT NULL,
    client_message              TEXT         NOT NULL,
    client_message_updated_at   DATETIME     NULL,
    worker_message              TEXT         NULL,
    worker_message_author       VARCHAR(100) NULL,
    worker_message_updated_at   DATETIME     NULL,
    predicted_completion_at     DATETIME     NULL,
    status                      VARCHAR(20)  NOT NULL,
    closed_at                   DATETIME     NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id              CHAR(36)     PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    hashed_password VARCHAR(255) NOT NULL,
    salt            VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Konta operatorów (autorzy odpowiedzi w zgłoszeniach poniżej).
-- Wygenerowane przez tools/SeedUserGenerator.java.
-- Hasło dla wszystkich trzech kont: Helpdesk1!
-- ON DUPLICATE KEY UPDATE: ponowne uruchomienie skryptu odświeży hasło/sól,
-- nie powielając kont (konflikt po unikalnym `username`).
-- ---------------------------------------------------------------------------
INSERT INTO users (id, username, hashed_password, salt, role) VALUES
    ('a6314284-2049-44a7-a62f-b897bbe88883', 'k.nowak',       '5/ZV26FzsP9CBJJdfaT0tV+lROPruc+6rgTwcxZAwLw=', 'tDsnaBC3Fhc4i8RZIq8ruQ==', 'OPERATOR'),
    ('95d2b380-cf87-4390-a19f-2c8eff53e599', 'm.lewandowski', 'x0Ybcpc1eqzyrwi8Ec2bzCWC0sgqJU4clnlUaovS0ng=', '1SssRdMULoDLjRwuSSLIgQ==', 'OPERATOR'),
    ('8ea5d4a3-9ac8-4409-9992-386036d31743', 'a.zielinska',   'wYrhl2jDrq2rGwtCSEVaWZymlYWhZSEUumCRgqajWY8=', 'boakEwJGw0vKovVMbY5KHQ==', 'OPERATOR')
ON DUPLICATE KEY UPDATE
    hashed_password = VALUES(hashed_password),
    salt            = VALUES(salt),
    role            = VALUES(role);

-- UWAGA: poniższe czyści WSZYSTKIE istniejące zgłoszenia, aby ponowne
-- uruchomienie skryptu dawało powtarzalny, znany stan. Zakomentuj tę linię,
-- jeśli chcesz tylko DOŁOŻYĆ dane do już istniejących zgłoszeń.
-- (Tabeli `users` nie czyścimy — INSERT wyżej działa w trybie upsert.)
TRUNCATE TABLE tickets;

-- --- NOT_STARTED: przyjęte, jeszcze bez reakcji pracownika ---------------
INSERT INTO tickets
    (created_at, first_name, last_name, client_message,
     client_message_updated_at, worker_message, worker_message_author,
     worker_message_updated_at, predicted_completion_at, status, closed_at)
VALUES
    ('2026-06-15 09:12:00', 'Żaneta', 'Żak',
     'Nie mogę zalogować się do panelu klienta — strona zwraca błąd 500.',
     NULL, NULL, NULL, NULL, NULL, 'NOT_STARTED', NULL),
    ('2026-06-14 17:40:00', 'Łukasz', 'Wójcik',
     'Faktura za maj zawiera błędną kwotę VAT — proszę o korektę.',
     NULL, NULL, NULL, NULL, NULL, 'NOT_STARTED', NULL),
    ('2026-06-13 12:00:00', 'Michał', 'Dąbrowski',
     'Aplikacja mobilna zawiesza się przy starcie na Androidzie 14.',
     NULL, NULL, NULL, NULL, NULL, 'NOT_STARTED', NULL);

-- --- IN_PROGRESS: pracownik odpowiedział, termin w przyszłości -----------
INSERT INTO tickets
    (created_at, first_name, last_name, client_message,
     client_message_updated_at, worker_message, worker_message_author,
     worker_message_updated_at, predicted_completion_at, status, closed_at)
VALUES
    ('2026-06-10 11:05:00', 'Małgorzata', 'Ćwikła',
     'Laptop nie ładuje się mimo podłączonej ładowarki.',
     NULL,
     'Przyjęliśmy zgłoszenie, prosimy o numer seryjny urządzenia.',
     'k.nowak', '2026-06-11 08:30:00', '2026-06-20 17:00:00', 'IN_PROGRESS', NULL),
    ('2026-06-08 14:22:00', 'Krzysztof', 'Jabłoński',
     'Drukarka sieciowa nie odpowiada po aktualizacji firmware.',
     NULL,
     'Zamówiliśmy część zamienną, oczekujemy dostawy.',
     'm.lewandowski', '2026-06-09 10:00:00', '2026-06-25 12:00:00', 'IN_PROGRESS', NULL),
    ('2026-06-02 09:00:00', 'Katarzyna', 'Kowalczyk',
     'System raportów generuje pusty PDF. (Zaktualizowano: błąd tylko w Chrome.)',
     '2026-06-04 18:30:00',
     'Poprosiliśmy o logi przeglądarki, analizujemy problem.',
     'a.zielinska', '2026-06-05 09:00:00', '2026-06-22 16:00:00', 'IN_PROGRESS', NULL);

-- --- DELAYED: pracownik odpowiedział, termin już minął -------------------
INSERT INTO tickets
    (created_at, first_name, last_name, client_message,
     client_message_updated_at, worker_message, worker_message_author,
     worker_message_updated_at, predicted_completion_at, status, closed_at)
VALUES
    ('2026-05-28 10:00:00', 'Agnieszka', 'Śliwińska',
     'Monitor migocze po około 10 minutach pracy.',
     NULL,
     'Diagnostyka w toku, skontaktujemy się wkrótce.',
     'k.nowak', '2026-05-30 09:00:00', '2026-06-10 12:00:00', 'DELAYED', NULL),
    ('2026-05-25 14:30:00', 'Halina', 'Sędziwój',
     'Strona firmowa ładuje się bardzo wolno w godzinach porannych.',
     NULL,
     'Eskalowano do zespołu serwerowego.',
     'm.lewandowski', '2026-05-27 10:00:00', '2026-06-12 09:00:00', 'DELAYED', NULL),
    ('2026-05-20 08:15:00', 'Tomasz', 'Główka',
     'Licencja oprogramowania wygasła mimo opłaconej subskrypcji.',
     NULL,
     'Czekamy na potwierdzenie od producenta oprogramowania.',
     'admin', '2026-05-22 12:00:00', '2026-06-05 10:00:00', 'DELAYED', NULL);

-- --- COMPLETED: zgłoszenia zamknięte -------------------------------------
INSERT INTO tickets
    (created_at, first_name, last_name, client_message,
     client_message_updated_at, worker_message, worker_message_author,
     worker_message_updated_at, predicted_completion_at, status, closed_at)
VALUES
    ('2026-05-15 10:10:00', 'Andrzej', 'Król',
     'Proszę o zwrot środków za niedostarczony towar.',
     NULL,
     'Zwrot środków zrealizowany na konto klienta.',
     'a.zielinska', '2026-05-16 11:00:00', '2026-05-19 12:00:00', 'COMPLETED',
     '2026-05-18 09:40:00'),
    ('2026-05-05 09:30:00', 'Barbara', 'Łęcka',
     'Komputer nie uruchamia się — słychać powtarzające się sygnały dźwiękowe.',
     NULL,
     'Wymieniono dysk, urządzenie sprawne. Zgłoszenie zamknięte.',
     'k.nowak', '2026-05-09 14:00:00', '2026-05-12 12:00:00', 'COMPLETED',
     '2026-05-12 15:20:00'),
    ('2026-04-28 13:45:00', 'Paweł', 'Górniak',
     'Konto zostało zablokowane po kilku nieudanych próbach logowania.',
     NULL,
     'Zresetowano hasło i odblokowano konto.',
     'admin', '2026-04-29 09:10:00', '2026-05-02 12:00:00', 'COMPLETED',
     '2026-05-01 11:00:00'),
    ('2026-04-22 16:05:00', 'Zofia', 'Wiśniewska',
     'Poczta służbowa nie wysyła wiadomości z załącznikami.',
     NULL,
     'Naprawiono konfigurację serwera SMTP, działa poprawnie.',
     'm.lewandowski', '2026-04-24 08:45:00', '2026-04-27 12:00:00', 'COMPLETED',
     '2026-04-27 10:30:00');

-- Podsumowanie: 3 operatorów (k.nowak, m.lewandowski, a.zielinska; hasło
-- Helpdesk1!) oraz 13 zgłoszeń (3x NOT_STARTED, 3x IN_PROGRESS, 3x DELAYED,
-- 4x COMPLETED). Autorami odpowiedzi są operatorzy i 'admin'. Jedno zgłoszenie
-- (Kowalczyk) ma ustawione client_message_updated_at — demonstruje edycję
-- treści przez klienta.
