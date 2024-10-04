package com.org.project.component;

import com.org.project.util.CustomFlywayMigrationStrategy;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomFlywayMigrationStrategyTest {

    private CustomFlywayMigrationStrategy migrationStrategy;
    private Flyway mockFlyway;
    private MigrationInfoService mockInfoService;
    private MigrationInfo validMigrationInfo;
    private MigrationInfo invalidMigrationInfo;

    @BeforeEach
    public void setUp() {
        migrationStrategy = new CustomFlywayMigrationStrategy();
        mockFlyway = Mockito.mock(Flyway.class);
        mockInfoService = Mockito.mock(MigrationInfoService.class);

        when(mockFlyway.info()).thenReturn(mockInfoService);
    }

    @Test
    public void testMigrateWithValidMigrationFileName() {
        validMigrationInfo = mock(MigrationInfo.class);
        when(validMigrationInfo.getScript()).thenReturn("V2023_09_26_120000__initial.sql");
        when(mockInfoService.all()).thenReturn(new MigrationInfo[]{validMigrationInfo});

        assertDoesNotThrow(() -> migrationStrategy.migrate(mockFlyway));

        verify(mockFlyway).migrate();
    }

    @Test
    public void testMigrateWithInvalidMigrationFileName() {
        invalidMigrationInfo = mock(MigrationInfo.class);
        when(invalidMigrationInfo.getScript()).thenReturn("V2023-09-26-12000__initial.sql");
        when(mockInfoService.all()).thenReturn(new MigrationInfo[]{invalidMigrationInfo});

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            migrationStrategy.migrate(mockFlyway);
        });

        assertEquals("Invalid migration file name: V2023-09-26-12000__initial.sql. Expected format: VYYYY_MM_DD_HHMMSS__name.sql", thrown.getMessage());

        verify(mockFlyway, never()).migrate();
    }
}
