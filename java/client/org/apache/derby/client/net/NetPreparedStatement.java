/*

   Derby - Class org.apache.derby.client.net.NetPreparedStatement

   Copyright (c) 2001, 2005 The Apache Software Foundation or its licensors, where applicable.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/
package org.apache.derby.client.net;

import org.apache.derby.client.am.ColumnMetaData;
import org.apache.derby.client.am.PreparedStatement;
import org.apache.derby.client.am.Section;
import org.apache.derby.client.am.SqlException;
import org.apache.derby.jdbc.ClientDriver;
import org.apache.derby.client.am.ClientJDBCObjectFactory;
import org.apache.derby.client.ClientPooledConnection;


public class NetPreparedStatement extends NetStatement
        implements org.apache.derby.client.am.MaterialPreparedStatement {

    // Alias for (NetPreparedStatement) super.statement.
    /*final*/
    org.apache.derby.client.am.PreparedStatement preparedStatement_;


    // Relay constructor for NetCallableStatement.
    NetPreparedStatement(org.apache.derby.client.am.PreparedStatement statement,
                         NetAgent netAgent,
                         NetConnection netConnection) {
        super(statement, netAgent, netConnection);
        initNetPreparedStatement(statement);
    }

    void resetNetPreparedStatement(org.apache.derby.client.am.PreparedStatement statement,
                                   NetAgent netAgent,
                                   NetConnection netConnection) {
        super.resetNetStatement(statement, netAgent, netConnection);
        initNetPreparedStatement(statement);
    }

    private void initNetPreparedStatement(org.apache.derby.client.am.PreparedStatement statement) {
        preparedStatement_ = statement;
        preparedStatement_.materialPreparedStatement_ = this;
    }

    /**
     *
     * The constructor for the NetPreparedStatement class. Called by abstract
     * Connection.prepareStatment().newPreparedStatement()
     * for jdbc 2 prepared statements with scroll attributes.
     * It has the ClientPooledConnection as one of its parameters
     * this is used to raise the Statement Events when the prepared
     * statement is closed.
     *
     * @param agent The instance of NetAgent associated with this
     *              CallableStatement object.
     * @param netNonnection The connection object associated with this
     *                      PreparedStatement Object.
     * @param sql         A String object that is the SQL statement
     *                    to be sent to the database.
     * @param type        One of the ResultSet type constants.
     * @param concurrency One of the ResultSet concurrency constants.
     * @param holdability One of the ResultSet holdability constants.
     * @param autoGeneratedKeys a flag indicating whether auto-generated
     *                          keys should be returned.
     * @param columnNames A String array of column names indicating
     *                    the columns that should be returned
     *                    from the inserted row or rows.
     * @param cpc The ClientPooledConnection wraps the underlying physical
     *            connection associated with this prepared statement
     *            it is used to pass the Statement closed and the Statement
     *            error occurred events that occur back to the
     *            ClientPooledConnection.
     * @returns a PreparedStatement object
     * @throws SqlException
     *
     */
    NetPreparedStatement(NetAgent netAgent, NetConnection netConnection, String sql, int type, int concurrency, int holdability, int autoGeneratedKeys, String[] columnNames, ClientPooledConnection cpc) throws SqlException {
        this(ClientDriver.getFactory().newPreparedStatement(netAgent,
                netConnection, sql, type, concurrency, holdability,
                autoGeneratedKeys, columnNames, cpc),
                netAgent,
                netConnection
                );
    }

    void resetNetPreparedStatement(NetAgent netAgent, NetConnection netConnection, String sql, int type, int concurrency, int holdability, int autoGeneratedKeys, String[] columnNames) throws SqlException {
        preparedStatement_.resetPreparedStatement(netAgent, netConnection, sql, type, concurrency, holdability, autoGeneratedKeys, columnNames);
        resetNetPreparedStatement(preparedStatement_, netAgent, netConnection);
    }

    /**
     *
     * The constructor for the NetPreparedStatement class. For JDBC 3.0 
     * positioned updates.It has the ClientPooledConnection as one of 
     * its parameters this is used to raise the Statement Events when the 
     * prepared statement is closed.
     *
     * @param agent The instance of NetAgent associated with this
     *              CallableStatement object.
     * @param netNonnection The connection object associated with this 
     *                      PreparedStatement Object. 
     * @param sql           A String object that is the SQL statement to be 
     *                      sent to the database.
     * @param section Section
     * @param cpc The ClientPooledConnection wraps the underlying physical 
     *            connection associated with this prepared statement 
     *            it is used to pass the Statement closed and the Statement 
     *            error occurred events that occur back to the 
     *            ClientPooledConnection
     * @throws SqlException
     *
     */
    NetPreparedStatement(NetAgent netAgent,
                         NetConnection netConnection,
                         String sql,
                         Section section,ClientPooledConnection cpc) 
                         throws SqlException {
        this(ClientDriver.getFactory().newPreparedStatement(netAgent,
                netConnection, sql, section,cpc),
                netAgent,
                netConnection);
    }

    void resetNetPreparedStatement(NetAgent netAgent,
                                   NetConnection netConnection,
                                   String sql,
                                   Section section) throws SqlException {
        preparedStatement_.resetPreparedStatement(netAgent, netConnection, sql, section);
        resetNetPreparedStatement(preparedStatement_, netAgent, netConnection);
    }

    void resetNetPreparedStatement(NetAgent netAgent,
                                   NetConnection netConnection,
                                   String sql,
                                   Section section,
                                   ColumnMetaData parameterMetaData,
                                   ColumnMetaData resultSetMetaData) throws SqlException {
        preparedStatement_.resetPreparedStatement(netAgent, netConnection, sql, section, parameterMetaData, resultSetMetaData);
        this.resetNetPreparedStatement(preparedStatement_, netAgent, netConnection);
    }

    protected void finalize() throws java.lang.Throwable {
        super.finalize();
    }

    public void writeExecute_(Section section,
                              ColumnMetaData parameterMetaData,
                              Object[] inputs,
                              int numInputColumns,
                              boolean outputExpected,
                              // This is a hint to the material layer that more write commands will follow.
                              // It is ignored by the driver in all cases except when blob data is written,
                              // in which case this boolean is used to optimize the implementation.
                              // Otherwise we wouldn't be able to chain after blob data is sent.
                              // If we could always chain a no-op DDM after every execute that writes blobs
                              // then we could just always set the chaining flag to on for blob send data
                              boolean chainedWritesFollowingSetLob) throws SqlException {
        netAgent_.statementRequest_.writeExecute(this,
                section,
                parameterMetaData,
                inputs,
                numInputColumns,
                outputExpected,
                chainedWritesFollowingSetLob);
    }


    public void readExecute_() throws SqlException {
        netAgent_.statementReply_.readExecute(preparedStatement_);
    }

    public void writeOpenQuery_(Section section,
                                int fetchSize,
                                int resultSetType,
                                int numInputColumns,
                                ColumnMetaData parameterMetaData,
                                Object[] inputs) throws SqlException {
        netAgent_.statementRequest_.writeOpenQuery(this,
                section,
                fetchSize,
                resultSetType,
                numInputColumns,
                parameterMetaData,
                inputs);
    }
    // super.readOpenQuery()

    public void writeDescribeInput_(Section section) throws SqlException {
        netAgent_.statementRequest_.writeDescribeInput(this, section);
    }

    public void readDescribeInput_() throws SqlException {
        netAgent_.statementReply_.readDescribeInput(preparedStatement_);
    }

    public void writeDescribeOutput_(Section section) throws SqlException {
        netAgent_.statementRequest_.writeDescribeOutput(this, section);
    }

    public void readDescribeOutput_() throws SqlException {
        netAgent_.statementReply_.readDescribeOutput(preparedStatement_);
    }
}
