ALTER TABLE file
ADD COLUMN `updated_user_id` CHAR(36) NULL AFTER `creation_user_id`;

ALTER TABLE file
ADD CONSTRAINT `fk_file_updated_user_id` FOREIGN KEY (`updated_user_id`) REFERENCES `user` (`id`);

ALTER TABLE file_content_relation
DROP FOREIGN KEY `FK_file_content_relation_updated_user_id`;

ALTER TABLE file_content_relation
DROP COLUMN `updated_user_id`;
