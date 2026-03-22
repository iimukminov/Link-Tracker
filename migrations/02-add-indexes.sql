CREATE INDEX idx_link_last_update ON link(last_update);

CREATE INDEX idx_link_chat_link_id ON link_chat(link_id);
CREATE INDEX idx_link_tag_link_id ON link_tag(link_id);
CREATE INDEX idx_link_tag_tag_id ON link_tag(tag_id);
