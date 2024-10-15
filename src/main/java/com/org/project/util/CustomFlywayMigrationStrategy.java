package com.org.project.util;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;

import java.util.regex.Pattern;

public class CustomFlywayMigrationStrategy implements FlywayMigrationStrategy {

    private static final Pattern MIGRATION_FILE_PATTERN = Pattern.compile("V(\\d{4})_(\\d{2})_(\\d{2})_(\\d{6})__.*\\.sql");

    @Override
    public void migrate(Flyway flyway) {
        for (MigrationInfo info : flyway.info().all()) {
            String scriptName = info.getScript();
            if (!MIGRATION_FILE_PATTERN.matcher(scriptName).matches()) {
                throw new IllegalStateException("Invalid migration file name: " + scriptName +
                        ". Expected format: VYYYY_MM_DD_HHMMSS__name.sql");
            }
        }
        flyway.migrate();
    }
}