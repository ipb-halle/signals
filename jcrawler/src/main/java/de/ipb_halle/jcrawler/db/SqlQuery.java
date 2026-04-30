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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fblocal
 */
public abstract class SqlQuery<T> extends SqlCommand implements Iterator<T> {

    private enum ResultState {
        UNPREPARED,
        EXECUTED,
        PREPARED_CLOSED;
    }

    protected SqlQuery(String query) {
        super(query);
    }

    private ResultSet result;
    private ResultState state = ResultState.UNPREPARED;
    private boolean validRecord = false;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected abstract T getRecord() throws SQLException;

    public abstract QueryType getType();

    public void execute(T obj) throws SQLException {
        throw new UnsupportedOperationException("execute(T) not implemented for this query");
    }

    @Override
    public void execute(List<Object> parameters) throws SQLException {
        if (state != ResultState.PREPARED_CLOSED) {
            throw new IllegalStateException("SqlQuery is not in PREPARED_CLOSED state");
        }
        setParameters(parameters);
        result = getStatement().executeQuery();
        validRecord = result.next();
        state = ResultState.EXECUTED;
    }

    @Override
    public void close() throws SQLException {
        if (state != ResultState.EXECUTED) {
            throw new IllegalStateException("SqlQuery is not in EXECUTED state");
        }
        state = ResultState.PREPARED_CLOSED;
        result.close();
        super.close();
    }

    public void prepare(Connection conn) throws SQLException {
        setStatement(conn.prepareStatement(getQuery()));
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
                logger.warn(e.getMessage());
                // fall through
            }
        }
        throw new NoSuchElementException("no result available");
    }

    protected ResultSet getResultSet() {
        return result;
    }

    @Override
    protected void setStatement(PreparedStatement st) {
        super.setStatement(st);
        state = ResultState.PREPARED_CLOSED;
    }

    private void nextRecord() throws SQLException {
        try {
            validRecord = result.next();
        } catch( SQLException e) {
            validRecord = false;
        }
    }
}
