ALTER TABLE file_content_relation
ADD COLUMN updated_user_id CHAR(36) NOT NULL,
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
ADD CONSTRAINT FK_file_content_relation_updated_user_id FOREIGN KEY (updated_user_id) REFERENCES user (id);

ALTER TABLE file
ADD COLUMN creation_user_id CHAR(36) NOT NULL,
ADD CONSTRAINT FK_file_creation_user_id FOREIGN KEY (creation_user_id) REFERENCES user (id);
