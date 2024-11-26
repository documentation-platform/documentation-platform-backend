ALTER TABLE folder
ADD COLUMN user_id CHAR(36) DEFAULT NULL AFTER `organization_id`;

ALTER TABLE folder
ADD CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE folder
ADD CONSTRAINT chk_organization_or_user
CHECK (
  (organization_id IS NOT NULL AND user_id IS NULL) OR
  (organization_id IS NULL AND user_id IS NOT NULL)
);