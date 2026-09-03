package com.hencjo.summer.migration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.Test;

public class DatabaseTest {
	private static final String NUMBER_OF_TABLES_SQL =
			"SELECT count(*) as c FROM pg_tables WHERE schemaname=current_schema();";

	@Test
	public void returnsTheNumberOfTablesInTheCurrentSchema() throws Exception {
		Connection connection = connectionReturningTableCount(3);

		assertEquals(3, new Database().numberOfTables(connection));
	}

	@Test
	public void containsTablesReturnsFalseForAnEmptyCurrentSchema() throws Exception {
		Connection connection = connectionReturningTableCount(0);

		assertFalse(new Database().containsTables(connection));
	}

	@Test
	public void containsTablesReturnsTrueForANonEmptyCurrentSchema() throws Exception {
		Connection connection = connectionReturningTableCount(1);

		assertTrue(new Database().containsTables(connection));
	}

	private Connection connectionReturningTableCount(int numberOfTables) throws Exception {
		Connection connection = mock(Connection.class);
		Statement statement = mock(Statement.class);
		ResultSet resultSet = mock(ResultSet.class);
		when(connection.createStatement()).thenReturn(statement);
		when(statement.executeQuery(NUMBER_OF_TABLES_SQL)).thenReturn(resultSet);
		when(resultSet.getInt(1)).thenReturn(numberOfTables);
		return connection;
	}
}
