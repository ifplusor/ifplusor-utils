package psn.ifplusor.persistence.jdbc;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author james
 * @version 10/12/16
 */
public class JdbcDao<T> {

    private psn.ifplusor.persistence.jdbc.JdbcDaoDelegate<T> delegate;

    private Connection conn = null; // 保持长连接

    public JdbcDao(Connection conn, Class<T> clazz) {
        this(conn, null, clazz);
    }

    public JdbcDao(Connection conn, psn.ifplusor.persistence.jdbc.JdbcDaoDelegate<T> delegate) {
        this(conn, delegate, null);
    }

    private JdbcDao(Connection conn, psn.ifplusor.persistence.jdbc.JdbcDaoDelegate<T> delegate, Class<T> clazz) {

        if (conn == null || delegate == null && clazz == null) {
            throw new IllegalArgumentException("Encounter illegal parameter combination when construct JdbcDao.");
        }

        this.conn = conn;

        if (delegate != null) {
            this.delegate = delegate;
        } else {

            try {
                Class<? extends psn.ifplusor.persistence.jdbc.JdbcDaoDelegate> delegateClazz = (Class<? extends JdbcDaoDelegate>) Class.forName(clazz.getName() + "Delegate");
                this.delegate = delegateClazz.newInstance();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Class of \"" + clazz.getName() + "Delegate\" is missing!");
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public List<T> query(String table, String where, Integer limit) {
//		System.out.println("Creating statement...");

        if (table == null || table.trim().equals(""))
            throw new IllegalArgumentException();

        String sql = "select " + delegate.select().trim() + " from " + table.trim();
        if (where != null && !where.trim().equals(""))
            sql += " where " + where;
        if (limit != null && limit > 0)
            sql += " limit " + limit;

        List<T> list = new ArrayList<T>();

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            if (rs != null) {
                while (rs.next()) {
                    T obj = delegate.beanFromResultSet(rs);
                    if (obj != null) list.add(obj);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    public Object execute(String sql) {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            boolean bResultSet = stmt.execute(sql);

            if (bResultSet) {
                rs = stmt.getResultSet();
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();
                List<List<Object>> lstRs = new ArrayList<List<Object>>();
                while (rs.next()) {
                    List<Object> lst = new ArrayList<Object>();
                    for (int i = 0; i < columnCount; i++) {
                        switch (rsmd.getColumnType(i)) {
                            case Types.INTEGER:
                                lst.add(rs.getInt(i));
                            case Types.VARCHAR:
                                lst.add(rs.getString(i));
                            default:
                                lst.add(rs.getString(i));
                        }
                    }
                    lstRs.add(lst);
                }
                return lstRs;
            } else {
                return stmt.getUpdateCount();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
