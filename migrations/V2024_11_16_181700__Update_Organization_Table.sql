ALTER TABLE invite DROP FOREIGN KEY invite_ibfk_2;
ALTER TABLE folder DROP FOREIGN KEY folder_ibfk_2;
ALTER TABLE organization_user_relation DROP FOREIGN KEY organization_user_relation_ibfk_2;

ALTER TABLE invite MODIFY organization_id CHAR(36);
ALTER TABLE folder MODIFY organization_id CHAR(36);
ALTER TABLE organization_user_relation MODIFY organization_id CHAR(36);

ALTER TABLE organization MODIFY id CHAR(36);

ALTER TABLE invite
ADD CONSTRAINT invite_ibfk_2 FOREIGN KEY (organization_id) REFERENCES organization(id);

ALTER TABLE folder
ADD CONSTRAINT folder_ibfk_2 FOREIGN KEY (organization_id) REFERENCES organization(id);

ALTER TABLE organization_user_relation
ADD CONSTRAINT organization_user_relation_ibfk_2 FOREIGN KEY (organization_id) REFERENCES organization(id);
