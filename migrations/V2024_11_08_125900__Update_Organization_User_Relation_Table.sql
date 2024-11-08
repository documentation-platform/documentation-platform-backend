DROP TABLE IF EXISTS organization_user_relation;

CREATE TABLE organization_user_relation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    organization_id INT NOT NULL,
    access_id INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (organization_id) REFERENCES organization(id),
    FOREIGN KEY (access_id) REFERENCES access(id)
);