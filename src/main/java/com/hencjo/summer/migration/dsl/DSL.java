package com.hencjo.summer.migration.dsl;

import com.hencjo.summer.migration.api.Function;

public final class DSL {
	public static MigrationsDescription migrations(Migration ... migrations) {
		return new MigrationsDescription(migrations);
	}
	
	public static MigrationPart migration(String version) {
		return new MigrationPart(version);
	}
	
	public static ScriptUpgradeStep script(String scriptName) {
		return new ScriptUpgradeStep(scriptName);
	}

	public static FieldTransformation fieldTransformation(final String table, final String field, final Function<String,String> f) {
		return new FieldTransformation(table, field, f);
	}
}