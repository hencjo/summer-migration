package com.hencjo.summer.migration.dsl;

import com.hencjo.summer.migration.api.UpgradeStep;

public final class Baseline {
	public final String version;
	public final UpgradeStep[] upgradeSteps;

	protected Baseline(String version, UpgradeStep[] upgradeSteps) {
		this.version = version;
		this.upgradeSteps = upgradeSteps;
	}
}
