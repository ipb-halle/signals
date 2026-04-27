/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 Leibniz-Institut f. Pflanzenbiochemie
 *
 * JCrawler
 * JCrawler is a project to efficiently crawl large file systems.
 */
package de.ipb_halle.jcrawler.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.JDBCType;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 *
 * @author fblocal
 */
public abstract class SqlQuery<T> implements Iterator<T> {

    private enum ResultState {
        UNPREPARED,
        EXECUTED,
        PREPARED_CLOSED;
    }

    private PreparedStatement statement;
    private ResultSet result;
    private ResultState state = ResultState.UNPREPARED;
    private boolean validRecord = false;

    protected abstract T getRecord() throws SQLException;

    public abstract void prepare(Connection conn) throws SQLException;

    public abstract QueryType getType();

    public void execute(T obj) throws SQLException {
        throw new UnsupportedOperationException("execute(T) not implemented for this query");
    }

    public void execute(List<Object> parameters) throws SQLException {
        if (state != ResultState.PREPARED_CLOSED) {
            throw new IllegalStateException("SqlQuery is not in PREPARED_CLOSED state");
        }
        setParameters(parameters);
        result = statement.executeQuery();
        validRecord = result.next();
        state = ResultState.EXECUTED;
    }

    public void close() throws SQLException {
        if (state != ResultState.EXECUTED) {
            throw new IllegalStateException("SqlQuery is not in EXECUTED state");
        }
        state = ResultState.PREPARED_CLOSED;
        result.close();
        statement.clearParameters();
        statement.clearWarnings();
    }

    @Override
    public boolean hasNext() {
        if (state != ResultState.EXECUTED) {
            throw new IllegalStateException("SqlQuery is not in EXECUTED state");
        }
        return validRecord;
    }

    @Override
    public T next() {
        if (state != ResultState.EXECUTED) {
            throw new IllegalStateException("SqlQuery is not in EXECUTED state");
        }
        if (validRecord) {
            try {
                var t = getRecord();
                nextRecord();
                return t;
            } catch(SQLException e) {
                e.printStackTrace();
                // fall through
            }
        }
        throw new NoSuchElementException("no result available");
    }

    protected ResultSet getResultSet() {
        return result;
    }

    protected void setStatement(PreparedStatement st) {
        statement = st;
        state = ResultState.PREPARED_CLOSED;
    }

    private void nextRecord() throws SQLException {
        try {
            validRecord = result.next();
        } catch( SQLException e) {
            validRecord = false;
        }
    }

    private void setParameters(List<Object> parameters) throws SQLException {
        int i = 0;
        for (Object obj : parameters) {
            i++;
            if (obj == null) {
                throw new SQLException("parameter is null");
            }
            switch (obj.getClass().getName()) {
                case "[B" -> statement.setBytes(i, (byte[]) obj);
                case "java.lang.Boolean" -> statement.setBoolean(i, (Boolean) obj);
                case "java.lang.Integer" -> statement.setInt(i, (Integer) obj);
                case "java.lang.Long" -> statement.setLong(i, (Long) obj);
                case "java.lang.String" -> statement.setString(i, (String) obj);
                case "java.sql.Timestamp" -> statement.setTimestamp(i, (Timestamp) obj);
                case "de.ipb_halle.jcrawler.db.NullObj" -> statement.setNull(i,
                        ((NullObj) obj).getType().getVendorTypeNumber());
                default -> throw new RuntimeException("Unknown type: %s".formatted(obj.getClass().getName()));
            }
        }
    }

    public Object paramBoolean(Boolean param) {
        return (param == null) ? new NullObj(JDBCType.BOOLEAN) : param;
    }

    public Object paramBytes(byte[] param) {
        return (param == null) ? new NullObj(JDBCType.BINARY) : param;
    }

    public Object paramInteger(Integer param) {
        return (param == null) ? new NullObj(JDBCType.INTEGER) : param;
    }

    public Object paramLong(Long param) {
        return (param == null) ? new NullObj(JDBCType.BIGINT) : param;
    }

    public Object paramString(String param) {
        return (param == null) ? new NullObj(JDBCType.VARCHAR) : param;
    }

    public Object paramTimestamp(Timestamp param) {
        return (param == null) ? new NullObj(JDBCType.TIMESTAMP) : param;
    }
}
