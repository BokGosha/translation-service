CREATE TABLE IF NOT EXISTS translations (
    id BIGSERIAL PRIMARY KEY,
    client_ip VARCHAR(45),
    original_text TEXT,
    translated_text TEXT
);