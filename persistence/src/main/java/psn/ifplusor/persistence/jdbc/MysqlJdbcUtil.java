package psn.ifplusor.persistence.jdbc;

import java.sql.*;

/**
 * @author james
 * @version 10/20/16
 */
public class MysqlJdbcUtil {

    /**
     * 数据库驱动类名称
     */
    public static final String DRIVER = "com.mysql.jdbc.Driver";

    public static String getJdbcUrl(String ip, String port, String database, String other) {

        StringBuilder jdbcUrl = new StringBuilder();

        jdbcUrl.append("jdbc:mysql://").append(ip).append(":").append(port).append("/");

        if (database != null && database.trim().length() != 0)
            jdbcUrl.append(database.trim());

        if (other != null && other.trim().length() != 0)
            jdbcUrl.append("?").append(other.trim());

        return jdbcUrl.toString();
    }

    public static Connection getConnection(String ip, String port, String database, String username, String password) {
        try {
            // 加载数据库驱动程序
            Class.forName(DRIVER);

            String url = getJdbcUrl(ip, port, database, "useUnicode=true&characterEncoding=UTF8");

            // 获取连接
            DriverManager.setLoginTimeout(1);

            return DriverManager.getConnection(url, username, password);

        } catch (ClassNotFoundException e) {
            System.out.println("加载驱动错误");
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (null != rs) {
                rs.close();
            }

            if (null != stmt) {
                stmt.close();
            }

            if (null != conn) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
