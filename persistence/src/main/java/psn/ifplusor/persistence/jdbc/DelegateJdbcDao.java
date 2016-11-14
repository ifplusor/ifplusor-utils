package psn.ifplusor.persistence.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author james
 * @version 10/12/16
 */
public class DelegateJdbcDao<T> implements JdbcDao<T> {

    private final Logger logger = LoggerFactory.getLogger(DelegateJdbcDao.class);

    private final DataSource dataSource;
    private final JdbcDaoDelegate<T> delegate;
    private final ReflectJdbcDao<T> impl;

    public DelegateJdbcDao(DataSource dataSource, Class<T> clazz) {
        this(dataSource, null, clazz);
    }

    private DelegateJdbcDao(DataSource dataSource, JdbcDaoDelegate<T> delegate, Class<T> entityClazz) {

        if (entityClazz == null) {
            throw new IllegalArgumentException("Encounter illegal parameter when construct DelegateJdbcDao.");
        }

        if (delegate != null) {
            this.delegate = delegate;
        } else {

            try {
                Class<? extends JdbcDaoDelegate> delegateClazz =
                        (Class<? extends JdbcDaoDelegate>) Class.forName(entityClazz.getName() + "Delegate");
                this.delegate = delegateClazz.newInstance();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Class of \"" + entityClazz.getName() + "Delegate\" is missing!");
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        this.dataSource = dataSource;
        this.impl = new ReflectJdbcDao<T>(dataSource, entityClazz);
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

    public List<T> queryByParamWhere(String table, String where, List<List<?>> lstParams) {
        return impl.queryByParamWhere(table, where, lstParams);
    }

    public int insert(String table, T obj) {
        return impl.insert(table, obj);
    }

    public int insert(String table, List<T> lstObj) {
        return impl.insert(table, lstObj);
    }

    public int update(String table, T obj) {
        return impl.update(table, obj);
    }

    public int update(String table, List<T> lstObj) {
        return impl.update(table, lstObj);
    }

    public Object execute(String sql) throws SQLException {
        return impl.execute(sql);
    }
}
