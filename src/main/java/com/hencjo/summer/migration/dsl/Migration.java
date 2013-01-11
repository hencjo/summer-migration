package com.hencjo.summer.migration.dsl;

import com.hencjo.summer.migration.api.UpgradeStep;

public final class Migration {
	public final String key;
	public final UpgradeStep[] upgradeSteps;

	protected Migration(String from, UpgradeStep[] upgradeSteps) {
		this.key = from;
		this.upgradeSteps = upgradeSteps;
	}
}
