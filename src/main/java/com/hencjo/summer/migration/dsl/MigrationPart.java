package com.hencjo.summer.migration.dsl;

import com.hencjo.summer.migration.api.UpgradeStep;

public final class MigrationPart {
	private final String from;

	protected MigrationPart(String version) {
		this.from = version;
	}

	public Migration installsThrough(UpgradeStep ... upgradeSteps) {
		return new Migration(from, upgradeSteps);
	}
}
