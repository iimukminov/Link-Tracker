CREATE TABLE chat (
                      id BIGINT PRIMARY KEY,
                      created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE link (
                      id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                      url VARCHAR(2048) UNIQUE NOT NULL,
                      last_update TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE link_chat (
                           chat_id BIGINT REFERENCES chat(id) ON DELETE CASCADE,
                           link_id BIGINT REFERENCES link(id) ON DELETE CASCADE,
                           PRIMARY KEY (chat_id, link_id)
);

CREATE TABLE tag (
                     id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                     name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE link_tag (
                          link_id BIGINT REFERENCES link(id) ON DELETE CASCADE,
                          tag_id BIGINT REFERENCES tag(id) ON DELETE CASCADE,
                          PRIMARY KEY (link_id, tag_id)
);
