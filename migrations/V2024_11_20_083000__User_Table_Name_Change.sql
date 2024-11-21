RENAME TABLE users TO user;

ALTER TABLE organization_user_relation
DROP FOREIGN KEY organization_user_relation_ibfk_1;

ALTER TABLE organization_user_relation
ADD CONSTRAINT organization_user_relation_ibfk_1
FOREIGN KEY (user_id) REFERENCES user (id);
