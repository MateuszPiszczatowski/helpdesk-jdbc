-- Schema dla tabeli zgłoszeń. Aplikacja sama tworzy ją przy starcie
-- poprzez `infrastructure.persistence.DbConnection.initSchema()`.
-- Plik trzymany w repo dla dokumentacji / ręcznego setupu.

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

-- Konta pracowników (operatorzy + administrator). Tworzona przy starcie przez
-- DbConnection.initSchema(). Identyfikator to UUID generowany po stronie aplikacji.
CREATE TABLE IF NOT EXISTS users (
    id              CHAR(36)     PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    hashed_password VARCHAR(255) NOT NULL,
    salt            VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
