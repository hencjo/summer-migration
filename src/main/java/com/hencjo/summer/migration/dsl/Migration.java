package com.hencjo.summer.migration.dsl;

import com.hencjo.summer.migration.api.UpgradeStep;


public final class Migration {
	public final String from;
	public final String to;
	public final UpgradeStep[] upgradeSteps;

	protected Migration(String from, String to, UpgradeStep[] upgradeSteps) {
		this.from = from;
		this.to = to;
		this.upgradeSteps = upgradeSteps;
	}
}
