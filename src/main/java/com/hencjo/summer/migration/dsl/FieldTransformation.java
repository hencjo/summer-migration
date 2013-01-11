package com.hencjo.summer.migration.dsl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.hencjo.summer.migration.api.Function;
import com.hencjo.summer.migration.api.UpgradeStep;

public final class FieldTransformation implements UpgradeStep {
	private final String field;
	private final Function<String, String> f;
	private final String table;

	FieldTransformation(String table, String field, Function<String, String> f2) {
		this.field = field;
		this.f = f2;
		this.table = table;
	}

	@Override
	public void apply(Connection connection) throws IOException, SQLException {
		try (PreparedStatement ps = connection.prepareStatement("SELECT DISTINCT " + field + " FROM " + table + ";")) {
			try (ResultSet rs = ps.executeQuery()) {
				try (PreparedStatement ps2 = connection.prepareStatement("UPDATE workspaceaccesses SET " + field + " = ? WHERE " + field + " = ?;")) {
					while (rs.next()) {
						String value = rs.getString(1);
						ps2.setString(1, f.apply(value));
						ps2.setString(2, value);
						ps2.executeUpdate();
					}
				}
			}
		}
	}
}