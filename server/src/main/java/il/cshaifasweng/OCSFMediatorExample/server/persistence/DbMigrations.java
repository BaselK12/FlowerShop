package il.cshaifasweng.OCSFMediatorExample.server.persistence;

import org.flywaydb.core.Flyway;

public final class DbMigrations {
    private DbMigrations() {}

    public static void migrateFromEnv() {
        String url  = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASS");

        if (url == null || user == null || pass == null) {
            throw new IllegalStateException("DB_URL / DB_USER / DB_PASS must be set as environment variables");
        }

        Flyway.configure()
                .dataSource(url, user, pass)
                .baselineOnMigrate(true)
                .load()
                .migrate();
    }
}
