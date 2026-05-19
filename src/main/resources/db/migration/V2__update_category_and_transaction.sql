ALTER TABLE category DROP CONSTRAINT category_name_key;

ALTER TABLE category ADD COLUMN user_id BIGINT REFERENCES app_user(id) ON DELETE CASCADE DEFAULT NULL;

CREATE UNIQUE INDEX uq_system_category_name
    ON category (lower(name))
    WHERE user_id IS NULL;

CREATE UNIQUE INDEX uq_user_category_name
    ON category (user_id, lower(name))
    WHERE user_id IS NOT NULL;

ALTER TABLE transaction ADD COLUMN description VARCHAR(255);