package sql.client;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlDBConnection implements DataBaseConnection {
    private java.sql.Connection connection = null;

    @Override
    public void connect(String url) throws ConnectionException {
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new ConnectionException(e.getMessage());
        }
    }

    @Override
    public ResultSet executeQuery(String query) throws QueryException {
        try{
            return connection.createStatement().executeQuery(query);
        } catch (SQLException e) {
            throw new QueryException(e.getMessage());
        }
    }

    @Override
    public void executeUpdate(String update) throws QueryException {
        try{
            connection.createStatement().executeUpdate(update);
        } catch (SQLException e) {
            throw new QueryException(e.getMessage());
        }
    }
}
