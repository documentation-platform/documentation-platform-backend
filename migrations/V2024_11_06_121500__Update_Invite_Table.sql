drop table invite;

CREATE TABLE invite (
    id INT PRIMARY KEY AUTO_INCREMENT,
    access_id INT,
    organization_id INT,
    current_count INT DEFAULT 0,
    max_count INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME,

    FOREIGN KEY (access_id) REFERENCES access(id),
    FOREIGN KEY (organization_id) REFERENCES organization(id)
);