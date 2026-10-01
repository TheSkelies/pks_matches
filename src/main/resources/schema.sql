-- Удаление существующих таблиц и типов (для чистого перезапуска)
DROP TABLE IF EXISTS matches CASCADE;
DROP TABLE IF EXISTS tournaments CASCADE;
DROP TABLE IF EXISTS users CASCADE;

DROP TYPE IF EXISTS user_role;
DROP TYPE IF EXISTS match_status;
DROP TYPE IF EXISTS match_stage;

-- Создание перечислений (ENUM)
CREATE TYPE user_role AS ENUM ('ADMIN', 'USER', 'GUEST');
CREATE TYPE match_status AS ENUM ('SCHEDULED', 'LIVE', 'FINISHED', 'CANCELLED');
CREATE TYPE match_stage AS ENUM ('GROUP', 'QUARTER_FINAL', 'SEMI_FINAL', 'FINAL');

-- Таблица пользователей с проверкой уникальности логина и хэшированными паролями
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role user_role NOT NULL DEFAULT 'USER'
);

-- Таблица турниров с контролем корректности дат
CREATE TABLE tournaments (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    end_date DATE,
    CONSTRAINT chk_tournament_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

-- Таблица матчей с каскадным удалением, контролем разных команд и неотрицательного счета
CREATE TABLE matches (
    id SERIAL PRIMARY KEY,
    tournament_id INT NOT NULL,
    team1 VARCHAR(100) NOT NULL,
    team2 VARCHAR(100) NOT NULL,
    match_date TIMESTAMP NOT NULL,
    status match_status NOT NULL DEFAULT 'SCHEDULED',
    stage match_stage NOT NULL DEFAULT 'GROUP',
    score1 INT NOT NULL DEFAULT 0,
    score2 INT NOT NULL DEFAULT 0,
    FOREIGN KEY (tournament_id) REFERENCES tournaments(id) ON DELETE CASCADE,
    CONSTRAINT chk_different_teams CHECK (team1 <> team2),
    CONSTRAINT chk_valid_scores CHECK (score1 >= 0 AND score2 >= 0)
);

-- Индексы для ускорения поиска и фильтрации
CREATE INDEX IF NOT EXISTS idx_matches_tournament ON matches(tournament_id);
CREATE INDEX IF NOT EXISTS idx_matches_date ON matches(match_date);
CREATE INDEX IF NOT EXISTS idx_matches_status ON matches(status);

-- Начальные тестовые данные с криптографическими хэшами SHA-256
-- пароль admin: "admin" -> 8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918
-- пароль user:  "user"  -> 04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb
INSERT INTO users (username, password_hash, role) VALUES
    ('admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'ADMIN'),
    ('user', '04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb', 'USER');

INSERT INTO tournaments (name, start_date, end_date) VALUES
    ('ЧМ по футболу 2024', '2024-06-01', '2024-07-15');

INSERT INTO matches (tournament_id, team1, team2, match_date, status, stage, score1, score2) VALUES
    (1, 'Спартак', 'Зенит', '2024-06-10 19:00:00', 'SCHEDULED', 'GROUP', 0, 0),
    (1, 'ЦСКА', 'Динамо', '2024-06-12 20:00:00', 'FINISHED', 'QUARTER_FINAL', 2, 1);
