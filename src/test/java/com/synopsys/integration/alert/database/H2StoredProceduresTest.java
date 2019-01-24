package com.synopsys.integration.alert.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;

public class H2StoredProceduresTest {
    private Connection mockConnection;
    private Statement mockStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    public void mock() throws SQLException {
        mockConnection = Mockito.mock(Connection.class);
        mockStatement = Mockito.mock(Statement.class);
        mockResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(mockConnection.createStatement()).thenReturn(mockStatement);
        Mockito.when(mockStatement.executeQuery(Mockito.anyString())).thenReturn(mockResultSet);
    }

    @Test
    public void defineFieldTest() throws SQLException {
        Mockito.when(mockStatement.executeUpdate(Mockito.anyString())).thenReturn(1);
        try {
            H2StoredProcedures.defineField(mockConnection, "example.key", Boolean.FALSE, "example_descriptor", "EXAMPLE_CONTEXT");
        } catch (final SQLException e) {
            Assertions.fail("Expected the operation to not throw an exception");
        }
    }

    @Test
    public void defineFieldThrowsExceptionTest() throws SQLException {
        Mockito.when(mockStatement.executeUpdate(Mockito.anyString())).thenThrow(new SQLException("fake message"));
        try {
            H2StoredProcedures.defineField(mockConnection, "example.key", Boolean.FALSE, "example_descriptor", "EXAMPLE_CONTEXT");
            Assertions.fail("Expected exception to be thrown");
        } catch (final SQLException e) {
        }
    }

    @Test
    public void defineFieldIgnoresExceptionTest() throws SQLException {
        Mockito.when(mockStatement.executeUpdate(Mockito.contains("INSERT INTO ALERT.DEFINED_FIELDS"))).thenThrow(new SQLException(H2StoredProcedures.UNIQUENESS_CONTRAINT_MESSAGE_SEGMENT));
        Mockito.when(mockStatement.executeUpdate(Mockito.contains("INSERT INTO ALERT.FIELD_CONTEXTS"))).thenThrow(new SQLException(H2StoredProcedures.UNIQUENESS_CONTRAINT_MESSAGE_SEGMENT));
        Mockito.when(mockStatement.executeUpdate(Mockito.contains("INSERT INTO ALERT.DESCRIPTOR_FIELDS"))).thenReturn(1);
        try {
            H2StoredProcedures.defineField(mockConnection, "example.key", Boolean.FALSE, "example_descriptor", "EXAMPLE_CONTEXT");
        } catch (final SQLException e) {
            Assertions.fail("Expected the operation to not throw an exception");
        }
    }

    @Test
    public void getIdForRegisteredDescriptorNameTest() throws SQLException {
        final Integer id = 1;
        Mockito.when(mockResultSet.next()).thenReturn(Boolean.TRUE);
        Mockito.when(mockResultSet.getInt(Mockito.anyString())).thenReturn(id);

        final Integer retrievedId = H2StoredProcedures.getIdForRegisteredDescriptorName(mockConnection, "example");
        Assertions.assertEquals(id, retrievedId);
    }

    @Test
    public void getIdForRegisteredDescriptorNameExceptionTest() throws SQLException {
        Mockito.when(mockResultSet.next()).thenReturn(Boolean.FALSE);
        try {
            H2StoredProcedures.getIdForRegisteredDescriptorName(mockConnection, "example");
            Assertions.fail("Expected exception to be thrown");
        } catch (final SQLException e) {
        }
    }

    @Test
    public void getIdForDescriptorTypeTest() throws SQLException {
        final Integer id = 1;
        Mockito.when(mockResultSet.next()).thenReturn(Boolean.TRUE);
        Mockito.when(mockResultSet.getInt(Mockito.anyString())).thenReturn(id);

        final Integer retrievedId = H2StoredProcedures.getIdForDescriptorType(mockConnection, DescriptorType.COMPONENT.name());
        Assertions.assertEquals(id, retrievedId);
    }

    @Test
    public void getIdForDescriptorTypeThrowsExceptionTest() throws SQLException {
        Mockito.when(mockResultSet.next()).thenReturn(Boolean.FALSE);
        try {
            H2StoredProcedures.getIdForDescriptorType(mockConnection, DescriptorType.COMPONENT.name());
            Assertions.fail("Expected exception to be thrown");
        } catch (final SQLException e) {
        }
    }

    @Test
    public void getLatestFieldIdTest() throws SQLException {
        final Integer id = 1;
        Mockito.when(mockResultSet.next()).thenReturn(Boolean.TRUE);
        Mockito.when(mockResultSet.getInt(Mockito.anyString())).thenReturn(id);

        final Integer retrievedId = H2StoredProcedures.getLatestFieldId(mockConnection);
        Assertions.assertEquals(id, retrievedId);
    }

    @Test
    public void getLatestFieldIdThrowsExceptionTest() throws SQLException {
        Mockito.when(mockResultSet.next()).thenReturn(Boolean.FALSE);
        try {
            H2StoredProcedures.getLatestFieldId(mockConnection);
            Assertions.fail("Expected exception to be thrown");
        } catch (final SQLException e) {
        }
    }

    @Test
    public void getIdForConfigContextTest() throws SQLException {
        final Integer id = 1;
        Mockito.when(mockResultSet.next()).thenReturn(Boolean.TRUE);
        Mockito.when(mockResultSet.getInt(Mockito.anyString())).thenReturn(id);

        final Integer retrievedId = H2StoredProcedures.getIdForConfigContext(mockConnection, ConfigContextEnum.GLOBAL.name());
        Assertions.assertEquals(id, retrievedId);
    }

    @Test
    public void getIdForConfigContextThrowsExceptionTest() throws SQLException {
        Mockito.when(mockResultSet.next()).thenReturn(Boolean.FALSE);
        try {
            H2StoredProcedures.getIdForConfigContext(mockConnection, ConfigContextEnum.GLOBAL.name());
            Assertions.fail("Expected exception to be thrown");
        } catch (final SQLException e) {
        }
    }
}
