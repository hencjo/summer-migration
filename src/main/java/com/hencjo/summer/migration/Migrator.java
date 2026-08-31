package com.hencjo.summer.migration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;

import com.hencjo.summer.migration.api.UpgradeStep;
import com.hencjo.summer.migration.dsl.Migration;
import com.hencjo.summer.migration.dsl.MigrationsDescription;

public final class Migrator {
	private final SchemaMigrations schemaMigrations;
	private final Database database = new Database();
	
	public Migrator() {
		this.schemaMigrations = new SchemaMigrations("schema_migrations");
	}
	
	public void migrate(Connection connection, MigrationsDescription upgradeDescription) throws IOException, SQLException {
		connection.setAutoCommit(false);
		
		if (!database.containsTables(connection))
			schemaMigrations.create(connection);
			
		if (!schemaMigrations.exists(connection)) 
			throw new RuntimeException("Expected " + schemaMigrations.tableName + " to exist and contain the version number. It doesn't.");
		
		migrations(connection, upgradeDescription.migrations);
	}

	private void migrations(Connection connection, Migration[] migrations) throws SQLException, IOException {
		for (Migration migration : migrations) {
			if (schemaMigrations.isApplied(connection, migration.key)) continue;
			System.out.println("Applying migration \"" + migration.key + "\" ... ");
			Instant start = Instant.now();
			for (UpgradeStep upgradeStep : migration.upgradeSteps) upgradeStep.apply(connection);
			Duration duration = Duration.between(start, Instant.now());
			System.out.printf("Migration \"%s\" completed in %d.%03ds%n", migration.key, duration.getSeconds(), duration.getNano() / 1_000_000);
			schemaMigrations.addApplied(connection, migration.key);
			connection.commit();
		}
	}
}
