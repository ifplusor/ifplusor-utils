package psn.ifplusor.persistence.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author james
 * @version 10/21/16
 */
public class SqliteJdbcUtil {

    public static final String DRIVER = "org.sqlite.JDBC";

    public static String getJdbcUrl(String path) {

        StringBuilder jdbcUrl = new StringBuilder();

        jdbcUrl.append("jdbc:sqlite:").append(path);

        return jdbcUrl.toString();
    }

    public static Connection getConnection(String path) {
        try {
            // 加载数据库驱动程序
            Class.forName(DRIVER);

            String url = getJdbcUrl(path);

            // 获取连接
//            DriverManager.setLoginTimeout(1);

            return DriverManager.getConnection(url);

        } catch (ClassNotFoundException e) {
            System.out.println("加载驱动错误");
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
