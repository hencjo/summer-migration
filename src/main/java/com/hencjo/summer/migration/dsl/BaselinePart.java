package com.hencjo.summer.migration.dsl;

import com.hencjo.summer.migration.api.UpgradeStep;

public final class BaselinePart {
	private final String version;

	protected BaselinePart(String version) {
		this.version = version;
	}

	public Baseline installsThrough(UpgradeStep ... upgradeSteps) {
		return new Baseline(version, upgradeSteps);
	}
}