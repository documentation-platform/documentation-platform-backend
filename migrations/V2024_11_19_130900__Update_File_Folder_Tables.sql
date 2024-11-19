ALTER TABLE file DROP FOREIGN KEY file_ibfk_1;
ALTER TABLE folder DROP FOREIGN KEY folder_ibfk_1;
ALTER TABLE folder DROP FOREIGN KEY folder_ibfk_2;

ALTER TABLE folder
    MODIFY COLUMN id CHAR(36) NOT NULL,
    MODIFY COLUMN parent_id CHAR(36) NULL,
    MODIFY COLUMN organization_id CHAR(36) NULL;

ALTER TABLE file
    MODIFY COLUMN id CHAR(36) NOT NULL,
    MODIFY COLUMN folder_id CHAR(36) NOT NULL;

ALTER TABLE file_content_relation
    MODIFY COLUMN id CHAR(36) NOT NULL;

ALTER TABLE folder
    ADD CONSTRAINT fk_parent_id FOREIGN KEY (parent_id) REFERENCES folder(id),
    ADD CONSTRAINT fk_organization_id FOREIGN KEY (organization_id) REFERENCES organization(id);

ALTER TABLE file
    ADD CONSTRAINT fk_folder_id FOREIGN KEY (folder_id) REFERENCES folder(id);