package com.hencjo.summer.migration;

import java.sql.*;

public class SchemaMigrations {
	public final String tableName;

	public SchemaMigrations(String tableName) {
		this.tableName = tableName;
	}

	public void create(Connection connection) throws SQLException {
		try (Statement s = connection.createStatement()) {
			s.executeUpdate("CREATE TABLE " + tableName + " (id text PRIMARY KEY NOT NULL CHECK (id <> ''), installed timestamp DEFAULT now());");
		}
	}

	public void addApplied(Connection connection, String key) throws SQLException {
		try (PreparedStatement p = connection.prepareStatement("INSERT INTO " + tableName + " (id) VALUES (?);")) {
			p.setString(1, key);
			p.executeUpdate();
		}		
	}

	public boolean isApplied(Connection connection, String key) throws SQLException {
		try (PreparedStatement p = connection.prepareStatement("SELECT count(id) FROM " + tableName + " WHERE id = ?;")) {
			p.setString(1, key);
			try (ResultSet r = p.executeQuery()) {
				r.next();
				return r.getInt(1) == 1;
			}
		}
	}

	public boolean exists(Connection connection) throws SQLException {
		try (PreparedStatement ps = connection.prepareStatement("select count(tablename) from pg_tables where schemaname='public' AND tablename=?;")) {
			ps.setString(1, tableName);
			try (ResultSet r = ps.executeQuery()) {
				r.next();
				return r.getInt(1) == 1;
			}
		}
	}
}