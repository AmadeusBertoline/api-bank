-- 1. Tabela de Log de Auditoria
CREATE TABLE log_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conta_id BIGINT NOT NULL,
    status_anterior VARCHAR(50),
    status_novo VARCHAR(50),
    usuario_alteracao VARCHAR(150),
    data_alteracao DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. Trigger de Auditoria
CREATE TRIGGER trg_log_status_conta
AFTER UPDATE ON contas
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO log_status (
            conta_id,
            status_anterior,
            status_novo,
            usuario_alteracao
        )
        VALUES (
            NEW.id,
            OLD.status,
            NEW.status,
            @usuario_logado
        );
    END IF;
END;