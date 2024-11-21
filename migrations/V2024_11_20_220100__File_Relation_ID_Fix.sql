ALTER TABLE file_content_relation
DROP PRIMARY KEY;

ALTER TABLE file_content_relation
CHANGE COLUMN id file_id CHAR(36) NOT NULL;

ALTER TABLE file_content_relation
ADD PRIMARY KEY (file_id);

ALTER TABLE file_content_relation
ADD CONSTRAINT FK_file_content_relation_file_id FOREIGN KEY (file_id) REFERENCES file (id);
