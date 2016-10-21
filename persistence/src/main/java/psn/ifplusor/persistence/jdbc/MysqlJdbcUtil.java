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
	private static final String DRIVER = "com.mysql.jdbc.Driver";

	public static Connection getConnection(String ip, String port, String database, String username, String password) {
		try {
			// 加载数据库驱动程序
			Class.forName(DRIVER);

			String url = "jdbc:mysql://" + ip + ":" + port + "/" + database + "?"
					+ "user=" + username + "&password=" + password
					+ "&useUnicode=true&characterEncoding=UTF8";

			// 获取连接
			DriverManager.setLoginTimeout(1);

			return DriverManager.getConnection(url);

		} catch (ClassNotFoundException e) {
			System.out.println("加载驱动错误");
			System.out.println(e.getMessage());
		}catch (SQLException e) {
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
