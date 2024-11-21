ALTER TABLE `organization_user_relation`
DROP FOREIGN KEY `organization_user_relation_ibfk_1`;

ALTER TABLE `users`
CHANGE COLUMN `id` `id` CHAR(36) NOT NULL;

ALTER TABLE `organization_user_relation`
CHANGE COLUMN `user_id` `user_id` CHAR(36) NOT NULL;

ALTER TABLE `organization_user_relation`
ADD CONSTRAINT `organization_user_relation_ibfk_1`
FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
