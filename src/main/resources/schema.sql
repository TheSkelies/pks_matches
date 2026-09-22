-- Удаление таблиц, если они существуют (для чистого перезапуска)
DROP TABLE IF EXISTS matches;
DROP TABLE IF EXISTS tournaments;
DROP TABLE IF EXISTS users;

-- Таблица пользователей
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL DEFAULT 'USER' -- ADMIN, USER, GUEST
);

-- Таблица турниров
CREATE TABLE tournaments (
                             id SERIAL PRIMARY KEY,
                             name VARCHAR(100) NOT NULL UNIQUE,
                             start_date DATE NOT NULL,
                             end_date DATE
);

-- Таблица матчей
CREATE TABLE matches (
                         id SERIAL PRIMARY KEY,
                         tournament_id INT NOT NULL,
                         team1 VARCHAR(100) NOT NULL,
                         team2 VARCHAR(100) NOT NULL,
                         match_date TIMESTAMP NOT NULL,
                         status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED', -- SCHEDULED, LIVE, FINISHED, CANCELLED
                         score1 INT DEFAULT 0,
                         score2 INT DEFAULT 0,
                         FOREIGN KEY (tournament_id) REFERENCES tournaments(id) ON DELETE CASCADE,
                         CONSTRAINT chk_different_teams CHECK (team1 <> team2)
);

-- Начальные тестовые данные (для проверки)
INSERT INTO users (username, password_hash, role) VALUES
                                                      ('admin', 'admin_hash', 'ADMIN'),
                                                      ('user1', 'user1_hash', 'USER');

INSERT INTO tournaments (name, start_date, end_date) VALUES
    ('ЧМ по футболу 2024', '2024-06-01', '2024-07-15');

INSERT INTO matches (tournament_id, team1, team2, match_date, status, score1, score2) VALUES
                                                                                          (1, 'Спартак', 'Зенит', '2024-06-10 19:00:00', 'SCHEDULED', 0, 0),
                                                                                          (1, 'ЦСКА', 'Динамо', '2024-06-12 20:00:00', 'FINISHED', 2, 1);