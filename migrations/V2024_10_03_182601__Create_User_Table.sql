CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    provider ENUM('google', 'github', 'local') NOT NULL,
    password_hash VARCHAR(255) NULL,
    auth_version INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE email_provider_key (email, provider),
    CHECK (
        (provider = 'local' AND password_hash IS NOT NULL) OR
        (provider != 'local' AND password_hash IS NULL)
    )
);
