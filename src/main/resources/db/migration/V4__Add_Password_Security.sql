-- Adiciona coluna de data de alteração de senha
ALTER TABLE users ADD COLUMN password_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Cria tabela de histórico de senhas
CREATE TABLE password_histories (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_history_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Index para busca rápida por usuário
CREATE INDEX idx_password_histories_user_id ON password_histories(user_id);
