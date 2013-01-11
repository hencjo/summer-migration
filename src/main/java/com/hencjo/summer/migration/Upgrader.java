package com.hencjo.summer.migration;

import java.io.IOException;
import java.sql.*;
import com.hencjo.summer.migration.api.UpgradeStep;
import com.hencjo.summer.migration.dsl.Baseline;
import com.hencjo.summer.migration.dsl.Migration;
import com.hencjo.summer.migration.dsl.UpgradeDescription;

public final class Upgrader {
	private final SchemaVersion schemaVersion;
	private final Database database = new Database();
	
	public Upgrader() {
		this.schemaVersion = new SchemaVersion("schemaversion");
	}
	
	public void upgrade(Connection connection, UpgradeDescription upgradeDescription) throws IOException, SQLException {
		connection.setAutoCommit(false);
		
		if (!database.containsTables(connection))
			installBaseline(connection, upgradeDescription.baseline, schemaVersion);
		if (!schemaVersion.get(connection).isPresent()) 
			throw new RuntimeException("Expected " + schemaVersion.tableName + " to exist and contain the version number. It doesn't.");
		
		migrations(connection, upgradeDescription.migrations);
	}

	private void migrations(Connection connection, Migration[] migrations) throws SQLException, IOException {
		for (Migration migration : migrations) {
			if (!migration.from.equals(schemaVersion.get(connection).get())) continue;
			for (UpgradeStep upgradeStep : migration.upgradeSteps) upgradeStep.apply(connection);	
			schemaVersion.set(connection, migration.to);
			connection.commit();
			System.out.println("Upgraded to " + migration.to + ".");
		}
	}

	private void installBaseline(Connection connection, Baseline baseline, SchemaVersion schemaVersion) throws IOException, SQLException {
		for (UpgradeStep upgradeStep : baseline.upgradeSteps) upgradeStep.apply(connection);
		schemaVersion.create(connection, baseline.version);
		connection.commit();
		System.out.println("Installed baseline for " + baseline.version + ".");
	}
}
