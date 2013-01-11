package com.hencjo.summer.migration.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Scripts {
	public static void executeScript(Connection connection, String filename) throws IOException, SQLException {
		String readLines = Resources.toString(Resources.getResource(filename), Charsets.UTF8);
		for (String sql : readLines.split(";")) {
			try (Statement s = connection.createStatement()) {
				s.executeUpdate(sql);
			}
		}
	}
}
