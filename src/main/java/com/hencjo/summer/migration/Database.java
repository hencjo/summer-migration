package com.hencjo.summer.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
	private static final String MIGRATION_LOCK_NAME = "summer-migration";

	void lockMigrations(Connection connection) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement("SELECT pg_advisory_xact_lock(hashtext(?));")) {
			statement.setString(1, MIGRATION_LOCK_NAME);
			statement.execute();
		}
	}

	public boolean containsTables(Connection connection) throws SQLException {
		return numberOfTables(connection) > 0;
	}

	int numberOfTables(Connection connection) throws SQLException {
		try (Statement s = connection.createStatement()) {
			try (ResultSet r = s.executeQuery("SELECT count(*) as c FROM pg_tables WHERE schemaname=current_schema();")) {
				r.next();
				return r.getInt(1);
			}
		}
	}
}
