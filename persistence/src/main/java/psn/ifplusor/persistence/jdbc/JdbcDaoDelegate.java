package psn.ifplusor.persistence.jdbc;

import java.sql.ResultSet;

/**
 * @author james
 * @version 10/20/16
 */
public interface JdbcDaoDelegate <T> {

    String select();

    T beanFromResultSet(ResultSet rs);
}
