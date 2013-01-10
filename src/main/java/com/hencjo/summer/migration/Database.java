package com.hencjo.summer.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
	public boolean containsTables(Connection connection) throws SQLException {
		try (Statement s = connection.createStatement()) {
			try (ResultSet r = s.executeQuery("SELECT count(*) as c FROM pg_tables WHERE schemaname='public';")) {
				r.next();
				int numberOfTables = r.getInt(1);
				return numberOfTables > 0;
			}
		}
	}
}