package psn.ifplusor.persistence.jdbc;

import java.sql.SQLException;
import java.util.List;

/**
 * @author james
 * @version 11/3/16
 */
public interface JdbcDao <T> {

    List<T> query(String table, String where, Integer limit);

    int insert(String table, T obj);
    int insert(String table, List<T> lstObj);

    int update(String table, T obj);
    int update(String table, List<T> lstObj);

    Object execute(String sql) throws SQLException;
}
