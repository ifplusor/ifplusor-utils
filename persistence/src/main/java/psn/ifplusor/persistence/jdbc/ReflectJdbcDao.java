package psn.ifplusor.persistence.jdbc;

import psn.ifplusor.persistence.entity.EntityUtil;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author james
 * @version 10/12/16
 */
public class ReflectJdbcDao<T> implements JdbcDao<T> {

    protected final DataSource dataSource;
    protected final Class<T> entityClazz;

    public ReflectJdbcDao(DataSource dataSource, Class<T> entityClazz) {

        if (dataSource == null) {
            throw new IllegalArgumentException("Encounter illegal parameter combination when construct ReflectJdbcDao.");
        }

        this.dataSource = dataSource;
        this.entityClazz = entityClazz;
    }

    public List<T> query(String table, String where, Integer limit) {
//		System.out.println("Creating statement...");

        if (table == null || table.trim().equals(""))
            throw new IllegalArgumentException();

        List<T> list = new ArrayList<T>();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String sql = EntityUtil.genQuerySqlWithParams(table, where, limit, null, entityClazz);

            conn = dataSource.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            if (rs != null) {
                while (rs.next()) {
                    T obj = EntityUtil.beanFromResultSet(rs, entityClazz);
                    if (obj != null) list.add(obj);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
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

    public int insert(String table, T obj) {
        List<T> lst = new ArrayList<T>();
        lst.add(obj);
        return insert(table, lst);
    }

    public int insert(String table, List<T> lstObj) {
        Connection conn = null;
        PreparedStatement stmt = null;
        int count = 0;

        try {
            String sql = EntityUtil.genInsertSqlWithParams(table, null, entityClazz);
            List<EntityUtil.Property> lstProperties = EntityUtil.getColumnProperties(entityClazz);

            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(sql);

            for (T obj : lstObj) {
                int index = 1;
                for (EntityUtil.Property property : lstProperties) {
                    stmt.setObject(index++, property.getGetter().invoke(obj));
                }
                count += stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
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

        return count;
    }

    public int update(String table, T obj) {
        List<T> lst = new ArrayList<T>();
        lst.add(obj);
        return update(table, lst);
    }

    public int update(String table, List<T> lstObj) {
        Connection conn = null;
        PreparedStatement stmt = null;
        int count = 0;

        try {
            EntityUtil.Property id = EntityUtil.getIdProperty(entityClazz);
            Set<String> exColumn = new HashSet<String>();
            exColumn.add(id.getColumn());
            String sql = EntityUtil.genUpdateSqlWithParams(table,
                    id.getColumn() + "=?", exColumn, entityClazz);
            List<EntityUtil.Property> lstProperties = EntityUtil.getColumnProperties(entityClazz);

            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(sql);

            for (T obj : lstObj) {
                int index = 1;
                for (EntityUtil.Property property : lstProperties) {
                    if (exColumn.contains(property.getColumn())) continue;
                    stmt.setObject(index++, property.getGetter().invoke(obj));
                }
                stmt.setObject(index, id.getGetter().invoke(obj));
                count += stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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

        return count;
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
