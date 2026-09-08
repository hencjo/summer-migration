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

		try {
			database.lockMigrations(connection);
			int numberOfTables = database.numberOfTables(connection);
			if (numberOfTables == 0) {
				schemaMigrations.create(connection);
			} else if (!schemaMigrations.exists(connection)) {
				String tables = numberOfTables == 1 ? "table" : "tables";
				throw new RuntimeException("The current schema contains " + numberOfTables + " " + tables + " but is missing table '" + schemaMigrations.tableName + "'.");
			}
			connection.commit();
		} catch (SQLException | RuntimeException e) {
			rollback(connection, e);
			throw e;
		}

		migrations(connection, upgradeDescription.migrations);
	}

	private void migrations(Connection connection, Migration[] migrations) throws SQLException, IOException {
		for (Migration migration : migrations) {
			Instant start;
			try {
				database.lockMigrations(connection);
				if (schemaMigrations.isApplied(connection, migration.key)) {
					connection.commit();
					continue;
				}
				System.out.println("Applying migration \"" + migration.key + "\" ... ");
				start = Instant.now();
				for (UpgradeStep upgradeStep : migration.upgradeSteps) upgradeStep.apply(connection);
				schemaMigrations.addApplied(connection, migration.key);
				connection.commit();
			} catch (IOException | SQLException | RuntimeException e) {
				rollback(connection, e);
				throw e;
			}
			Duration duration = Duration.between(start, Instant.now());
			System.out.printf("Migration \"%s\" completed in %d.%03ds%n", migration.key, duration.getSeconds(), duration.getNano() / 1_000_000);
		}
	}

	private void rollback(Connection connection, Exception failure) {
		try {
			connection.rollback();
		} catch (SQLException rollbackFailure) {
			failure.addSuppressed(rollbackFailure);
		}
	}
}
