package com.hencjo.summer.migration;

import static com.hencjo.summer.migration.dsl.DSL.migration;
import static com.hencjo.summer.migration.dsl.DSL.migrations;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.Test;
import org.mockito.InOrder;

import com.hencjo.summer.migration.api.UpgradeStep;

public class MigratorTest {
	private static final String NUMBER_OF_TABLES_SQL =
			"SELECT count(*) as c FROM pg_tables WHERE schemaname=current_schema();";
	private static final String CREATE_SCHEMA_MIGRATIONS_SQL =
			"CREATE TABLE schema_migrations (id text PRIMARY KEY NOT NULL CHECK (id <> ''), installed timestamp DEFAULT now());";
	private static final String SCHEMA_MIGRATIONS_EXISTS_SQL =
			"select count(tablename) from pg_tables where schemaname=current_schema() AND tablename=?;";
	private static final String IS_APPLIED_SQL =
			"SELECT count(id) FROM schema_migrations WHERE id = ?;";

	@Test
	public void commitsSchemaMigrationsForAnEmptyMigrationList() throws Exception {
		Connection connection = mock(Connection.class);
		Statement tableCountStatement = tableCountStatement(0);
		Statement createStatement = mock(Statement.class);
		when(connection.createStatement()).thenReturn(tableCountStatement, createStatement);

		new Migrator().migrate(connection, migrations());

		InOrder order = inOrder(createStatement, connection);
		order.verify(createStatement).executeUpdate(CREATE_SCHEMA_MIGRATIONS_SQL);
		order.verify(connection).commit();
	}

	@Test
	public void commitsSchemaMigrationsBeforeApplyingTheFirstMigration() throws Exception {
		Connection connection = mock(Connection.class);
		Statement tableCountStatement = tableCountStatement(0);
		Statement createStatement = mock(Statement.class);
		when(connection.createStatement()).thenReturn(tableCountStatement, createStatement);
		PreparedStatement isAppliedStatement = mock(PreparedStatement.class);
		ResultSet isAppliedResult = mock(ResultSet.class);
		when(connection.prepareStatement(IS_APPLIED_SQL)).thenReturn(isAppliedStatement);
		when(isAppliedStatement.executeQuery()).thenReturn(isAppliedResult);
		when(isAppliedResult.getInt(1)).thenReturn(0);
		UpgradeStep failingStep = mock(UpgradeStep.class);
		org.mockito.Mockito.doThrow(new IOException("migration failed")).when(failingStep).apply(connection);

		try {
			new Migrator().migrate(connection, migrations(migration("fails").installsThrough(failingStep)));
			fail("Expected the migration to fail");
		} catch (IOException expected) {
			assertEquals("migration failed", expected.getMessage());
		}

		InOrder order = inOrder(createStatement, connection, failingStep);
		order.verify(createStatement).executeUpdate(CREATE_SCHEMA_MIGRATIONS_SQL);
		order.verify(connection).commit();
		order.verify(failingStep).apply(connection);
	}

	@Test
	public void reportsTheTableCountWhenSchemaMigrationsIsMissing() throws Exception {
		Connection connection = mock(Connection.class);
		Statement tableCountStatement = tableCountStatement(5);
		when(connection.createStatement()).thenReturn(tableCountStatement);
		PreparedStatement existsStatement = mock(PreparedStatement.class);
		ResultSet existsResult = mock(ResultSet.class);
		when(connection.prepareStatement(SCHEMA_MIGRATIONS_EXISTS_SQL)).thenReturn(existsStatement);
		when(existsStatement.executeQuery()).thenReturn(existsResult);
		when(existsResult.getInt(1)).thenReturn(0);

		try {
			new Migrator().migrate(connection, migrations());
			fail("Expected migration bookkeeping to be required");
		} catch (RuntimeException expected) {
			assertEquals("The current schema contains 5 tables but is missing table 'schema_migrations'.", expected.getMessage());
		}

		verify(existsStatement).setString(1, "schema_migrations");
		verify(connection, never()).commit();
	}

	@Test
	public void usesSingularTableInTheMissingSchemaMigrationsMessage() throws Exception {
		Connection connection = mock(Connection.class);
		Statement tableCountStatement = tableCountStatement(1);
		when(connection.createStatement()).thenReturn(tableCountStatement);
		PreparedStatement existsStatement = mock(PreparedStatement.class);
		ResultSet existsResult = mock(ResultSet.class);
		when(connection.prepareStatement(SCHEMA_MIGRATIONS_EXISTS_SQL)).thenReturn(existsStatement);
		when(existsStatement.executeQuery()).thenReturn(existsResult);
		when(existsResult.getInt(1)).thenReturn(0);

		try {
			new Migrator().migrate(connection, migrations());
			fail("Expected migration bookkeeping to be required");
		} catch (RuntimeException expected) {
			assertEquals("The current schema contains 1 table but is missing table 'schema_migrations'.", expected.getMessage());
		}
	}

	private Statement tableCountStatement(int numberOfTables) throws Exception {
		Statement statement = mock(Statement.class);
		ResultSet resultSet = mock(ResultSet.class);
		when(statement.executeQuery(NUMBER_OF_TABLES_SQL)).thenReturn(resultSet);
		when(resultSet.getInt(1)).thenReturn(numberOfTables);
		return statement;
	}
}
