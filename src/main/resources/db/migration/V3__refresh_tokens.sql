-- Server-side refresh token store: turns refresh tokens from purely stateless
-- JWTs into revocable sessions. Each row is one active session (one device):
-- login inserts a row, refresh rotates it (delete old + insert new), logout
-- deletes it. Only a SHA-256 hash of the token is stored, never the token
-- itself, so a database leak does not hand out usable refresh tokens.
--
-- Column layout mirrors what Hibernate generates from RefreshTokenEntity so
-- that `ddl-auto: validate` in production matches exactly (see V1 header).
create table refresh_tokens (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    expires_at datetime(6) not null,
    token_hash varchar(255) not null,
    user_id bigint not null,
    primary key (id)
) engine=InnoDB;

alter table refresh_tokens
    add constraint uk_refresh_token_hash unique (token_hash);

create index idx_refresh_token_user on refresh_tokens (user_id);
