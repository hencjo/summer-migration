package com.hencjo.summer.migration.api;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public interface UpgradeStep {
	void apply(Connection connection) throws IOException, SQLException;
}
