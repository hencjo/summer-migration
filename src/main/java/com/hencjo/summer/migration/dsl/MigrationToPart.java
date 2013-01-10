package com.hencjo.summer.migration.dsl;

import com.hencjo.summer.migration.api.UpgradeStep;


public class MigrationToPart {
	private final String from;
	private final String to;

	protected MigrationToPart(String from, String to) {
		this.from = from;
		this.to = to;
	}

	public Migration through(UpgradeStep ... upgradeSteps) {
		return new Migration(from, to, upgradeSteps);
	}
}
