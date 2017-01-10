package psn.ifplusor.persistence.datasource;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteDataSource;
import psn.ifplusor.persistence.jdbc.MysqlJdbcUtil;
import psn.ifplusor.persistence.jdbc.SqliteJdbcUtil;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;

/**
 * @author james
 * @version 11/1/16
 */
public class DataSourceUtil {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceUtil.class);

    public static DataSource getMysqlDataSource(String ip, int port, String database, String username, String password)
            throws PropertyVetoException {

        if (logger.isDebugEnabled()) {
            logger.debug("Create C3P0 DataSource for MySQL. ip: {}, port: {}, database: {}, username: {}",
                    ip, port, database, username);
        }

        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(MysqlJdbcUtil.DRIVER);
        dataSource.setJdbcUrl(MysqlJdbcUtil.getJdbcUrl(ip, port, database,
                "useUnicode=true&characterEncoding=UTF8&rewriteBatchedStatements=true"));
        dataSource.setUser(username);
        dataSource.setPassword(password);

        // c3p0配置
        dataSource.setMaxIdleTime(300);
        dataSource.setPreferredTestQuery("SELECT 1");

        return dataSource;
    }

    /**
     * @param path the path of SQLite file.
     * @return return a DataSource which is a wrapper of Connection Factory.
     */
    public static DataSource getSqliteDataSource(final String path) throws PropertyVetoException {

        if (logger.isDebugEnabled()) {
            logger.debug("Create default DataSource for SQLite. path: {}", path);
        }

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(SqliteJdbcUtil.getJdbcUrl(path));
        return dataSource;
    }

    public static void close(DataSource dataSource) {

        if (logger.isDebugEnabled()) {
            logger.debug("Close DataSource. {}", dataSource.toString());
        }

        if (dataSource instanceof ComboPooledDataSource) {
            final ComboPooledDataSource comboPooledDataSource = (ComboPooledDataSource) dataSource;
            comboPooledDataSource.close();
        } else if (dataSource instanceof SQLiteDataSource) {
            final SQLiteDataSource sqliteDataSource = (SQLiteDataSource) dataSource;
        }
    }
}
