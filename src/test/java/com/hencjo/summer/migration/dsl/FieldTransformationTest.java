package com.hencjo.summer.migration.dsl;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.Test;
import org.mockito.InOrder;

import com.hencjo.summer.migration.api.Function;

public class FieldTransformationTest {
	@Test
	public void updatesTheConfiguredTableWithTheTransformedValue() throws Exception {
		Connection connection = mock(Connection.class);
		PreparedStatement selectStatement = mock(PreparedStatement.class);
		PreparedStatement updateStatement = mock(PreparedStatement.class);
		ResultSet resultSet = mock(ResultSet.class);
		@SuppressWarnings("unchecked")
		Function<String, String> transformation = mock(Function.class);
		when(connection.prepareStatement("SELECT DISTINCT email FROM accounts;")).thenReturn(selectStatement);
		when(connection.prepareStatement("UPDATE accounts SET email = ? WHERE email = ?;")).thenReturn(updateStatement);
		when(selectStatement.executeQuery()).thenReturn(resultSet);
		when(resultSet.next()).thenReturn(true, false);
		when(resultSet.getString(1)).thenReturn("old@example.com");
		when(transformation.apply("old@example.com")).thenReturn("new@example.com");

		new FieldTransformation("accounts", "email", transformation).apply(connection);

		verify(connection).prepareStatement("SELECT DISTINCT email FROM accounts;");
		verify(connection).prepareStatement("UPDATE accounts SET email = ? WHERE email = ?;");
		InOrder update = inOrder(updateStatement);
		update.verify(updateStatement).setString(1, "new@example.com");
		update.verify(updateStatement).setString(2, "old@example.com");
		update.verify(updateStatement).executeUpdate();
	}
}
