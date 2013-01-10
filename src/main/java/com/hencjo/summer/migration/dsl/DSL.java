package com.hencjo.summer.migration.dsl;

public final class DSL {
	public static UpgradeDescription upgradeDescription(Baseline baseline, Migration ... migrations) {
		return new UpgradeDescription(baseline, migrations);
	}

	public static BaselinePart baseline(String version) {
		return new BaselinePart(version);
	}
	
	public static MigrationFromPart version(String version) {
		return new MigrationFromPart(version);
	}
	
	public static ScriptUpgradeStep script(String scriptName) {
		return new ScriptUpgradeStep(scriptName);
	}
}
