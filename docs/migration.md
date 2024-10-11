# Flyway Migration Guide

This guide explains the Flyway migration structure used in our Spring Boot, MySQL, and Docker template repository.

## Overview

Flyway is a database migration tool that helps you version control your database schema. In our project, we use Flyway to manage and apply database migrations automatically.

## Migration Location

Our Flyway migrations are located in the `migrations/` directory at the root of the project. This location is specified in our Docker Compose files:

```yaml
SPRING_FLYWAY_LOCATIONS: filesystem:migrations
```

## Migration Naming Convention

Flyway migration files follow a specific naming convention:

```
V{version_date}__{description}.sql
```

- `V`: Indicates a versioned migration. You can also use `U` for undo migration and `R` for repeatable migration.
- `{version_date}`: The date of your migration change. This helps keep the migrations in a proper order (YYYY_MM_DD_HHMMSS)
- `__`: Double underscore separating version from description
- `{description}`: A brief description of the migration, using underscores for spaces

Example: `V2024_09_26_082319__create_users_table.sql`

## Migration Content

Each migration file contains SQL statements to modify the database schema. For example:

```sql
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Applying Migrations

Migrations are applied automatically when the application starts. Spring Boot's Flyway integration handles this process.

1. Flyway checks the current state of the database.
2. It looks for any new migration files that haven't been applied yet.
3. It applies these new migrations in order, based on their version numbers.

## Best Practices

1. **Never modify existing migrations**: Once a migration has been applied and committed, treat it as immutable. Create new migrations for further changes.

2. **Use meaningful descriptions**: The description in the filename should clearly indicate what the migration does.

3. **Keep migrations small and focused**: Each migration should do one thing, making it easier to understand and manage.

4. **Version control your migrations**: Always commit your migration files to your version control system.

5. **Test migrations**: Before applying migrations to production, test them thoroughly in a development or staging environment.

## Example Migration Sequence

Here's an example of how your migrations might evolve:

1. `V2024_09_26_082319__create_users_table.sql`
2. `V2024_09_26_134529__add_role_column_to_users.sql`
3. `V2024_09_27_063243_create_products_table.sql`
4. `V2024_09_28_223847__add_index_to_users_email.sql`

Each of these would contain the necessary SQL to make the described changes.

## Conclusion

By using Flyway, we ensure that our database schema is version controlled and that all environments (development, staging, production) stay in sync. This structure allows for easy tracking of database changes and simplifies the process of setting up new environments or updating existing ones.