package com.hencjo.summer.migration.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Scripts {
	public static void executeScript(Connection connection, String filename) throws IOException, SQLException {
		URL resource = Resources.getResource(filename);
		try (Scanner scanner = new Scanner(resource.openStream(), StandardCharsets.UTF_8.name()).useDelimiter("\\A")) {
			String sql = scanner.next();
			try (Statement s = connection.createStatement()) {
				s.executeUpdate(sql);
			}
		}
	}
}
