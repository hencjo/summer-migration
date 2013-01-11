package com.hencjo.summer.migration.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Scripts {
	public static void executeScript(Connection connection, String filename) throws IOException, SQLException {
  	URL resource = Resources.getResource(filename);
		try (InputStream openStream = resource.openStream();
				Scanner scanner2 = new Scanner(openStream, Charsets.UTF8.name());
				Scanner scanner = scanner2.useDelimiter(";")) {
			while (scanner.hasNext()) {
				String sql = scanner.next();
				try (Statement s = connection.createStatement()) {
					s.executeUpdate(sql);
				}
			}
		}
	}
}
