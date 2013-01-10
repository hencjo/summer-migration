package com.hencjo.summer.migration;

import java.sql.*;
import com.google.common.base.Optional;

public class SchemaVersion {
	public final String tableName;

	public SchemaVersion(String tableName) {
		this.tableName = tableName;
	}
	
	public Optional<String> get(Connection connection) throws SQLException {
		try (Statement s = connection.createStatement()) {
			try (ResultSet r = s.executeQuery("SELECT version FROM " + tableName + ";")) {
				if (!r.next()) return Optional.absent();
				return Optional.of(r.getString(1));
			}
		}
	}

	public void create(Connection connection, String version) throws SQLException {
		try (Statement s = connection.createStatement()) {
			s.executeUpdate("CREATE TABLE " + tableName + " (id integer PRIMARY KEY NOT NULL DEFAULT 0 CHECK (id = 0), version text NOT NULL);");
		}
		try (PreparedStatement p = connection.prepareStatement("INSERT INTO " + tableName + " (id, version) VALUES (0, ?);")) {
			p.setString(1, version);
			if (1 != p.executeUpdate()) throw new RuntimeException("Failed to insert version into " + tableName);
		}
	}

	public void set(Connection connection, String toVersion) throws SQLException {
		try (PreparedStatement prepareStatement = connection.prepareStatement("UPDATE " + tableName + " SET version = ?;")) {
			prepareStatement.setString(1, toVersion);
			int executeUpdate = prepareStatement.executeUpdate();
			if (executeUpdate != 1) throw new RuntimeException("Failed to update " + tableName + ".");
		}
	}
}
