package com.hencjo.summer.migration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

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
			long startNanos;
			try {
				database.lockMigrations(connection);
				if (schemaMigrations.isApplied(connection, migration.key)) {
					connection.commit();
					continue;
				}
				System.out.println("Applying migration \"" + migration.key + "\" ... ");
				startNanos = System.nanoTime();
				for (UpgradeStep upgradeStep : migration.upgradeSteps) upgradeStep.apply(connection);
				schemaMigrations.addApplied(connection, migration.key);
				connection.commit();
			} catch (IOException | SQLException | RuntimeException e) {
				rollback(connection, e);
				throw e;
			}
			long durationMillis = (System.nanoTime() - startNanos) / 1_000_000;
			System.out.printf("Migration \"%s\" completed in %d.%03ds%n", migration.key, durationMillis / 1_000, durationMillis % 1_000);
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
