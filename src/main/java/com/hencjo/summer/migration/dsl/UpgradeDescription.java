package com.hencjo.summer.migration.dsl;


public final class UpgradeDescription {
	public final Baseline baseline;
	public final Migration[] migrations;

	public UpgradeDescription(Baseline baseline, Migration[] migrations) {
		this.baseline = baseline;
		this.migrations = migrations;
	}
}
