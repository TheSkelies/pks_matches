-- V2__seed_data.sql
-- Тестовые данные для агрегатора матчей.

-- ==================== ТУРНИРЫ ====================
INSERT INTO tournaments (id, name, start_date, end_date, active) VALUES
    (uuid_generate_v4(), 'Чемпионат мира 2026', '2026-06-01', '2026-07-15', TRUE),
    (uuid_generate_v4(), 'Лига Чемпионов 2025/26', '2025-09-15', '2026-05-30', TRUE),
    (uuid_generate_v4(), 'Английская Премьер-лига 2025/26', '2025-08-10', '2026-05-20', TRUE),
    (uuid_generate_v4(), 'Кубок России 2025/26', '2025-07-20', '2026-05-25', TRUE);

-- ==================== ТЕГИ ====================
INSERT INTO tags (id, name) VALUES
    (uuid_generate_v4(), 'дерби'),
    (uuid_generate_v4(), 'плей-офф'),
    (uuid_generate_v4(), 'важный матч'),
    (uuid_generate_v4(), 'товарищеский');

-- ==================== МАТЧИ ====================
INSERT INTO matches (id, external_code, tournament_id, team1, team2, match_date, status, stage, score1, score2, priority, is_important, archived) 
SELECT 
    uuid_generate_v4(),
    'M-' || row_number() OVER (),
    (SELECT id FROM tournaments WHERE name = 'Чемпионат мира 2026'),
    team1, team2, match_date::timestamptz, status, stage, score1, score2, priority, is_important, archived
FROM (VALUES
    ('Аргентина', 'Франция', '2026-06-15 18:00:00+03', 'SCHEDULED', 'GROUP', 0, 0, 10, TRUE, FALSE),
    ('Бразилия', 'Германия', '2026-06-16 21:00:00+03', 'SCHEDULED', 'GROUP', 0, 0, 9, TRUE, FALSE),
    ('Испания', 'Англия', '2026-06-18 18:00:00+03', 'FINISHED', 'GROUP', 2, 1, 7, FALSE, FALSE),
    ('Португалия', 'Нидерланды', '2026-06-20 21:00:00+03', 'LIVE', 'GROUP', 1, 1, 8, FALSE, FALSE),
    ('Манчестер Сити', 'Реал Мадрид', '2026-02-15 22:00:00+03', 'FINISHED', 'QUARTER_FINAL', 3, 2, 10, TRUE, FALSE),
    ('Бавария', 'ПСЖ', '2026-02-16 22:00:00+03', 'SCHEDULED', 'SEMI_FINAL', 0, 0, 9, TRUE, FALSE),
    ('Спартак', 'Зенит', '2026-03-01 19:00:00+03', 'SCHEDULED', 'GROUP', 0, 0, 6, FALSE, FALSE),
    ('ЦСКА', 'Динамо', '2026-03-02 20:00:00+03', 'CANCELLED', 'GROUP', 0, 0, 5, FALSE, FALSE),
    ('Ливерпуль', 'Арсенал', '2026-04-10 19:30:00+03', 'SCHEDULED', 'GROUP', 0, 0, 8, FALSE, FALSE),
    ('Ювентус', 'Интер', '2025-12-10 22:00:00+03', 'FINISHED', 'FINAL', 1, 0, 10, TRUE, TRUE)
) AS v(team1, team2, match_date, status, stage, score1, score2, priority, is_important, archived);

-- ==================== ПРИВЯЗКА ТЕГОВ ====================
INSERT INTO match_tags (match_id, tag_id)
SELECT m.id, t.id
FROM matches m, tags t
WHERE t.name = 'дерби'
LIMIT 1;
