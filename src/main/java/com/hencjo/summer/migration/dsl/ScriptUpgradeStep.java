package com.hencjo.summer.migration.dsl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import com.hencjo.summer.migration.api.UpgradeStep;
import com.hencjo.summer.migration.util.Scripts;

public final class ScriptUpgradeStep implements UpgradeStep {
	private final String scriptName;

	protected ScriptUpgradeStep(String scriptName) {
		this.scriptName = scriptName;
	}

	@Override
	public void apply(Connection connection) throws IOException, SQLException {
		Scripts.executeScript(connection, scriptName);		
	}
}
