package com.hencjo.summer.migration.dsl;

public final class MigrationFromPart {
	private final String from;

	protected MigrationFromPart(String version) {
		this.from = version;
	}

	public MigrationToPart upgradesTo(String to) {
		return new MigrationToPart(from, to);
	}
}
