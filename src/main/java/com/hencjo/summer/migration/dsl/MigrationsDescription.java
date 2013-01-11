package com.hencjo.summer.migration.dsl;


public final class MigrationsDescription {
	public final Migration[] migrations;

	public MigrationsDescription(Migration[] migrations) {
		this.migrations = migrations;
	}
}
