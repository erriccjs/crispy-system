CREATE TABLE roles
(
    id   INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name VARCHAR(20),
    CONSTRAINT pk_roles PRIMARY KEY (id)
);

CREATE TABLE user_roles
(
    role_id INTEGER NOT NULL,
    user_id UUID    NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (role_id, user_id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_username UNIQUE (username);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_userol_on_role FOREIGN KEY (role_id) REFERENCES roles (id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_userol_on_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE transactions
ALTER
COLUMN amount TYPE DECIMAL USING (amount::DECIMAL);

ALTER TABLE accounts
ALTER
COLUMN balance TYPE DECIMAL USING (balance::DECIMAL);

ALTER TABLE transactions
ALTER
COLUMN balance_after_transaction TYPE DECIMAL USING (balance_after_transaction::DECIMAL);

ALTER TABLE users
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN password SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN username SET NOT NULL;