package psn.ifplusor.persistence.jdbc;

import psn.ifplusor.persistence.entity.EntityUtil;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author james
 * @version 10/12/16
 */
public class JdbcDao<T> {

    private JdbcDaoDelegate<T> delegate;

    private final DataSource dataSource;

    public JdbcDao(DataSource dataSource, Class<T> clazz) {
        this(dataSource, null, clazz);
    }

    public JdbcDao(DataSource dataSource, JdbcDaoDelegate<T> delegate) {
        this(dataSource, delegate, null);
    }

    private JdbcDao(DataSource dataSource, JdbcDaoDelegate<T> delegate, Class<T> clazz) {

        if (dataSource == null || delegate == null && clazz == null) {
            throw new IllegalArgumentException("Encounter illegal parameter combination when construct JdbcDao.");
        }

        this.dataSource = dataSource;

        if (delegate != null) {
            this.delegate = delegate;
        } else {

            try {
                Class<? extends JdbcDaoDelegate> delegateClazz = (Class<? extends JdbcDaoDelegate>) Class.forName(clazz.getName() + "Delegate");
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

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
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

            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    public int insert(String table, T obj, Class<T> clazz) {
        try {
            String sql = EntityUtil.genInsertSql(table, obj, clazz);
            return (Integer) execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int update(String table, T obj, Class<T> clazz) {
        try {
            EntityUtil.Property id = EntityUtil.getIdProperty(clazz);
            Set<String> exColumn = new HashSet<String>();
            exColumn.add(id.getColumn());
            String sql = EntityUtil.genUpdateSql(table,
                    id.getColumn() + "='" + id.getGetter().invoke(obj).toString() + "'",
                    exColumn, obj, clazz);
            return (Integer) execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public Object execute(String sql) throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
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
            throw e;
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

            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
