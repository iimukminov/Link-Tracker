INSERT INTO chat (id, created_at)
SELECT
    i,
    NOW()
FROM generate_series(1, 1000) AS i ON CONFLICT DO NOTHING;

INSERT INTO link (url, last_update, last_check_at)
SELECT
    'https://github.com/user/repo_' || i,
    NOW(),
    NOW()
FROM generate_series(1, 100000) AS i ON CONFLICT DO NOTHING;

INSERT INTO link_chat (chat_id, link_id)
SELECT
    ((i - 1) / 100) + 1,
    i
FROM generate_series(1, 100000) AS i ON CONFLICT DO NOTHING;
