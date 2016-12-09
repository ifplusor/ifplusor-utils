package psn.ifplusor.persistence.jdbc;

import java.sql.SQLException;
import java.util.List;

/**
 * @author james
 * @version 11/3/16
 */
public interface JdbcDao <T> {

    List<T> query(String table);
    List<T> query(String table, String where);
    List<T> query(String table, String where, String orderBy);
    List<T> query(String table, String where, String orderBy, String limit);

    List<T> queryByParamWhere(String table, String where, List<List<?>> lstParams);

    int insertOrUpdate(String table, T obj);
    int insertOrUpdate(String table, List<T> lstObj);

    int insert(String table, T obj);
    int insert(String table, List<T> lstObj);

    int update(String table, T obj);
    int update(String table, List<T> lstObj);

    Object execute(String sql) throws SQLException;
}
