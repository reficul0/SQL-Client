package sql.client;

import java.sql.ResultSet;

public interface DataBaseConnection {
    void connect(String url) throws ConnectionException;
    ResultSet executeQuery(String query) throws QueryException;
    void executeUpdate(String update) throws QueryException;
}
