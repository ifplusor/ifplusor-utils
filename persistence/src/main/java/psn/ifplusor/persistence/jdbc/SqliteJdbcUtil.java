package psn.ifplusor.persistence.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author james
 * @version 10/21/16
 */
public class SqliteJdbcUtil {

    private static final String DRIVER = "org.sqlite.JDBC";

    public static Connection getConnection(String path) {
        try {
            // 加载数据库驱动程序
            Class.forName(DRIVER);

            String url = "jdbc:sqlite:" + path;

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
