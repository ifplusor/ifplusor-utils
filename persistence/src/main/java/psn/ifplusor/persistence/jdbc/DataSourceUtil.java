package psn.ifplusor.persistence.jdbc;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;

/**
 * @author james
 * @version 11/1/16
 */
public class DataSourceUtil {

    public static DataSource getMysqlDataSource(String ip, String port, String database, String username, String password)
            throws PropertyVetoException {

        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(MysqlJdbcUtil.DRIVER);
        dataSource.setJdbcUrl(MysqlJdbcUtil.getJdbcUrl(ip, port, database, "useUnicode=true&characterEncoding=UTF8"));
        dataSource.setUser(username);
        dataSource.setPassword(password);

        return dataSource;
    }
}
