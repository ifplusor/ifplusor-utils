package psn.ifplusor.persistence.datasource;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import psn.ifplusor.persistence.jdbc.MysqlJdbcUtil;
import psn.ifplusor.persistence.jdbc.SqliteJdbcUtil;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * @author james
 * @version 11/1/16
 */
public class DataSourceUtil {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceUtil.class);

    public static DataSource getMysqlDataSource(String ip, String port, String database, String username, String password)
            throws PropertyVetoException {

        if (logger.isDebugEnabled()) {
            logger.debug("Create C3P0 DataSource for MySQL. ip: {}, port: {}, database: {}, username: {}",
                    ip, port, database, username);
        }

        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(MysqlJdbcUtil.DRIVER);
        dataSource.setJdbcUrl(MysqlJdbcUtil.getJdbcUrl(ip, port, database, "useUnicode=true&characterEncoding=UTF8"));
        dataSource.setUser(username);
        dataSource.setPassword(password);

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

        return new DataSource() {

            public Connection getConnection() throws SQLException {
                return SqliteJdbcUtil.getConnection(path);
            }

            public Connection getConnection(String s, String s1) throws SQLException {
                return null;
            }

            public <T> T unwrap(Class<T> aClass) throws SQLException {
                return null;
            }

            public boolean isWrapperFor(Class<?> aClass) throws SQLException {
                return false;
            }

            public PrintWriter getLogWriter() throws SQLException {
                return null;
            }

            public void setLogWriter(PrintWriter printWriter) throws SQLException {

            }

            public void setLoginTimeout(int i) throws SQLException {

            }

            public int getLoginTimeout() throws SQLException {
                return 0;
            }

            public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
                return null;
            }
        };
    }

    public static void close(DataSource dataSource) {

        if (logger.isDebugEnabled()) {
            logger.debug("Close DataSource. {}", dataSource.toString());
        }

        if (dataSource instanceof ComboPooledDataSource) {
            final ComboPooledDataSource comboPooledDataSource = (ComboPooledDataSource) dataSource;
            comboPooledDataSource.close();
        }
    }
}
