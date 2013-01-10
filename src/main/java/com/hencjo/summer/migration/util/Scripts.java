package com.hencjo.summer.migration.util;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class Scripts {
	public static void executeScript(Connection connection, String filename) throws IOException, SQLException {
		URL resource = Resources.getResource(filename);
		String readLines = Resources.toString(resource, Charsets.UTF_8);
		String[] sqls = readLines.split(";");
		
		for (String sql : sqls) {
			try (Statement s = connection.createStatement()) {
				s.executeUpdate(sql);
			}
		}
	}
}
