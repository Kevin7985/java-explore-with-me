CREATE TABLE IF NOT EXISTS users (
     id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
     email VARCHAR(255) NOT NULL,
     name VARCHAR(255) NOT NULL,
     role VARCHAR(50) NOT NULL,
     CONSTRAINT PK_USER PRIMARY KEY (id),
     CONSTRAINT UQ_USER_EMAIL UNIQUE (email),
     CONSTRAINT UQ_USER_NAME UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name VARCHAR(50),
    CONSTRAINT PK_CATEGORY PRIMARY KEY (id),
    CONSTRAINT UQ_CATEGORY_NAME UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS requests (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    requester_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT PK_REQUEST PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS events (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    title VARCHAR(120) NOT NULL,
    annotation VARCHAR(2000) NOT NULL,
    description VARCHAR(7000) NOT NULL,
    event_date TIMESTAMP WITHOUT TIME ZONE,
    latitude FLOAT NOT NULL,
    longitude FLOAT NOT NULL,
    initiator_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    paid BOOLEAN NOT NULL,
    participant_limit INTEGER NOT NULL,
    request_moderation BOOLEAN NOT NULL,
    state VARCHAR(50) NOT NULL,
    created_on TIMESTAMP WITHOUT TIME ZONE,
    published_on TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT PK_EVENT PRIMARY KEY (id),
    CONSTRAINT FK_EVENT_FOR_CATEGORY FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS compilations (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    title VARCHAR(50) NOT NULL,
    pinned BOOLEAN NOT NULL,
    CONSTRAINT PK_COMPILATION PRIMARY KEY (id),
    CONSTRAINT UQ_COMPILATION_TITLE UNIQUE (title)
);

CREATE TABLE IF NOT EXISTS compilations_events (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    compilation_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    CONSTRAINT PK_COMPILATIONS_EVENTS PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    CONSTRAINT PK_SUBSCRIPTIONS PRIMARY KEY (id),
    CONSTRAINT FK_FROM_USER_FOR_USER FOREIGN KEY (from_user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT FK_TO_USER_FOR_USER FOREIGN KEY (to_user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS feed (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    user_id BIGINT NOT NULL,
    feed_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    created TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT PK_FEED PRIMARY KEY (id),
    CONSTRAINT FK_USER_FOR_FEED FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);