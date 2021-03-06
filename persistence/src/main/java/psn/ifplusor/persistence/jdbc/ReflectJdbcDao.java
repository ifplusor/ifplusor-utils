package psn.ifplusor.persistence.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;
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

    Logger logger = LoggerFactory.getLogger(ReflectJdbcDao.class);

    protected final DataSource dataSource;
    protected final Class<T> entityClazz;

    public ReflectJdbcDao(DataSource dataSource, Class<T> entityClazz) {

        if (dataSource == null) {
            throw new IllegalArgumentException("Encounter illegal parameter combination when construct ReflectJdbcDao.");
        }

        this.dataSource = dataSource;
        this.entityClazz = entityClazz;
    }

    public List<T> query(String table) {
        return query(table, null, null, null);
    }

    public List<T> query(String table, String where) {
        return query(table, where, null, null);
    }

    public List<T> query(String table, String where, String orderBy) {
        return query(table, where, orderBy, null);
    }

    public List<T> query(String table, String where, String orderBy, String limit) {
//		System.out.println("Creating statement...");

        if (table == null || table.trim().equals(""))
            throw new IllegalArgumentException();

        List<T> list = new ArrayList<T>();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            String sql = EntityUtil.genQuerySqlWithParams(table, where, orderBy, limit, null, entityClazz);

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

    private static final int BATCH_SIZE[] = {51, 11, 4, 1};

    private static String replicateWhere(String where, int count) {

        if (count <= 1) {
            return where;
        }

        StringBuilder target = new StringBuilder();
        while (--count > 0) {
            target.append("(").append(where).append(") or ");
        }
        target.append("(").append(where).append(")");

        return target.toString();
    }

    public List<T> queryByParamWhere(String table, String where, List<List<?>> lstParams) {

        if (table == null || table.trim().equals("")
                || where == null || where.trim().equals("")
                || lstParams == null || lstParams.size() == 0) {
            throw new IllegalArgumentException();
        }

        List<T> list = new ArrayList<T>();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            int leave = lstParams.size();
            int idx = 0;

            conn = dataSource.getConnection();

            for (int i = 0; i < BATCH_SIZE.length; i++) {
                if (leave >= BATCH_SIZE[i]) {
                    try {
                        String sql = EntityUtil.genQuerySqlWithParams(table, replicateWhere(where, BATCH_SIZE[i]),
                                null, null, null, entityClazz);
                        stmt = conn.prepareStatement(sql);

                        while (leave >= BATCH_SIZE[i]) {
                            int index = 1;
                            for (int j = 0; j < BATCH_SIZE[i]; j++) {
                                List<?> params = lstParams.get(idx++);
                                for (Object param : params) {
                                    stmt.setObject(index++, param);
                                }
                            }

                            try {
                                rs = stmt.executeQuery();

                                if (rs != null) {
                                    while (rs.next()) {
                                        T obj = EntityUtil.beanFromResultSet(rs, entityClazz);
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
                            }

                            leave -= BATCH_SIZE[i];
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (stmt != null) {
                                stmt.close();
                                stmt = null;
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
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

            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    public int insertOrUpdate(String table, T obj) {
        List<T> lst = new ArrayList<T>();
        lst.add(obj);
        return insertOrUpdate(table, lst);
    }

    private int insertFailedThenUpdate(T obj, List<EntityUtil.Property> lstProperties, EntityUtil.Property id, Set<String> exColumn, PreparedStatement updateStmt)
            throws InvocationTargetException, IllegalAccessException, SQLException {
        int index = 1;
        for (EntityUtil.Property property : lstProperties) {
            if (exColumn.contains(property.getColumn())) continue;
            updateStmt.setObject(index++, property.getGetter().invoke(obj));
        }
        updateStmt.setObject(index, id.getGetter().invoke(obj));
        return updateStmt.executeUpdate();
    }

    public int insertOrUpdate(String table, List<T> lstObj) {
        if (lstObj.size() == 0) {
            return 0;
        }

        EntityUtil.Property id = null;
        try {
            id = EntityUtil.getIdProperty(entityClazz);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        if (id == null) {
            return insert(table, lstObj);
        } else {
            Connection conn = null;
            PreparedStatement insertStmt = null;
            PreparedStatement updateStmt = null;
            int count = 0;

            try {
                String insertSql = EntityUtil.genInsertSqlWithParams(table, null, entityClazz);

                Set<String> exColumn = new HashSet<String>();
                exColumn.add(id.getColumn());
                String updateSql = EntityUtil.genUpdateSqlWithParams(table,
                        id.getColumn() + "=?", exColumn, entityClazz);

                List<EntityUtil.Property> lstProperties = EntityUtil.getColumnProperties(entityClazz);

                conn = dataSource.getConnection();
                insertStmt = conn.prepareStatement(insertSql);
                updateStmt = conn.prepareStatement(updateSql);

                for (T obj : lstObj) {
                    int index = 1;
                    for (EntityUtil.Property property : lstProperties) {
                        insertStmt.setObject(index++, property.getGetter().invoke(obj));
                    }

                    try {
                        try {
                            count += insertStmt.executeUpdate();
                        } catch (SQLIntegrityConstraintViolationException e) {
                            // update
                            count += insertFailedThenUpdate(obj, lstProperties, id, exColumn, updateStmt);
                        } catch (SQLiteException e) {
                            if (!SQLiteErrorCode.SQLITE_CONSTRAINT_PRIMARYKEY.equals(e.getResultCode())) {
                                throw e;
                            }
                            count += insertFailedThenUpdate(obj, lstProperties, id, exColumn, updateStmt);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (insertStmt != null) insertStmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                try {
                    if (updateStmt != null) updateStmt.close();
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
    }

    public int insert(String table, T obj) {
        /*List<T> lst = new ArrayList<T>();
        lst.add(obj);
        return insert(table, lst);*/

        Connection conn = null;
        PreparedStatement stmt = null;
        int count = 0;

        try {
            String sql = EntityUtil.genInsertSqlWithParams(table, null, entityClazz);
            List<EntityUtil.Property> lstProperties = EntityUtil.getColumnProperties(entityClazz);

            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(sql);

            int index = 1;
            for (EntityUtil.Property property : lstProperties) {
                stmt.setObject(index++, property.getGetter().invoke(obj));
            }
            count += stmt.executeUpdate();
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

    public int insert(String table, List<T> lstObj) {
        if (lstObj.size() == 0) {
            return 0;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        int count = 0, noInfo = 0, failed = 0;

        try {
            String sql = EntityUtil.genInsertSqlWithParams(table, null, entityClazz);
            List<EntityUtil.Property> lstProperties = EntityUtil.getColumnProperties(entityClazz);

            conn = dataSource.getConnection();
            if (dataSource instanceof SQLiteDataSource) {
                /*
                   对SQLite的插入插入操作，使用事务会获得加速效果
                   > 关闭写同步可以进一步提高速度
                 */
                conn.setAutoCommit(false);
            }

            stmt = conn.prepareStatement(sql);

            int begin = 0, end = lstObj.size();
            while (begin < end) {
                for (int i = begin; i < end; i++) {
                    T obj = lstObj.get(i);
                    int index = 1;
                    for (EntityUtil.Property property : lstProperties) {
                        stmt.setObject(index++, property.getGetter().invoke(obj));
                    }
                    try {
                        stmt.addBatch();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                // 批量执行
                try {
                    for (int n : stmt.executeBatch()) {
                        if (n == Statement.SUCCESS_NO_INFO) {
                            noInfo++;
                        } else if (n == Statement.EXECUTE_FAILED) {
                            failed++;
                        } else {
                            count += n;
                        }
                    }
                    break;
                } catch (BatchUpdateException e) {
                    e.printStackTrace();

                    int[] rs = e.getUpdateCounts();
                    for (int n : rs) {
                        if (n == Statement.SUCCESS_NO_INFO) {
                            noInfo++;
                        } else if (n == Statement.EXECUTE_FAILED) {
                            failed++;
                        } else {
                            count += n;
                        }
                    }
                    begin += rs.length;
                    stmt.clearBatch();
                }
            }

            if (dataSource instanceof SQLiteDataSource) {
                conn.commit();
            }
        } catch (SQLException e) {
            if (dataSource instanceof SQLiteDataSource) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            if (dataSource instanceof SQLiteDataSource) {
                try {
                    if (conn != null) conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
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

        if (noInfo > 0 || failed > 0) {
            logger.warn("encounter error when excuteBatch. noInfo: {}, failed: {}.", noInfo, failed);
        }

        return count;
    }

    public int update(String table, T obj) {
        /*List<T> lst = new ArrayList<T>();
        lst.add(obj);
        return update(table, lst);*/

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

            int index = 1;
            for (EntityUtil.Property property : lstProperties) {
                if (exColumn.contains(property.getColumn())) continue;
                stmt.setObject(index++, property.getGetter().invoke(obj));
            }
            stmt.setObject(index, id.getGetter().invoke(obj));
            count += stmt.executeUpdate();
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

    public int update(String table, List<T> lstObj) {
        if (lstObj.size() == 0) {
            return 0;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        int count = 0, noInfo = 0, failed = 0;

        try {
            EntityUtil.Property id = EntityUtil.getIdProperty(entityClazz);
            Set<String> exColumn = new HashSet<String>();
            exColumn.add(id.getColumn());
            String sql = EntityUtil.genUpdateSqlWithParams(table,
                    id.getColumn() + "=?", exColumn, entityClazz);
            List<EntityUtil.Property> lstProperties = EntityUtil.getColumnProperties(entityClazz);

            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(sql);

            int begin = 0, end = lstObj.size();
            while (begin < end) {
                for (int i = begin; i < end; i++) {
                    T obj = lstObj.get(i);
                    int index = 1;
                    for (EntityUtil.Property property : lstProperties) {
                        if (exColumn.contains(property.getColumn())) continue;
                        stmt.setObject(index++, property.getGetter().invoke(obj));
                    }
                    stmt.setObject(index, id.getGetter().invoke(obj));
                    try {
                        stmt.addBatch();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                // 批量执行
                try {
                    for (int n : stmt.executeBatch()) {
                        if (n == Statement.SUCCESS_NO_INFO) {
                            noInfo++;
                        } else if (n == Statement.EXECUTE_FAILED) {
                            failed++;
                        } else {
                            count += n;
                        }
                    }
                    break;
                } catch (BatchUpdateException e) {
                    e.printStackTrace();

                    int[] rs = e.getUpdateCounts();
                    for (int n : rs) {
                        if (n == Statement.SUCCESS_NO_INFO) {
                            noInfo++;
                        } else if (n == Statement.EXECUTE_FAILED) {
                            failed++;
                        } else {
                            count += n;
                        }
                    }
                    begin += rs.length;
                    stmt.clearBatch();
                }
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

        if (noInfo > 0 || failed > 0) {
            logger.warn("encounter error when excuteBatch. noInfo: {}, failed: {}.", noInfo, failed);
        }

        return count;
    }

    public int delete(String table, T obj) {
        Connection conn = null;
        PreparedStatement stmt = null;
        int count = 0;

        try {
            EntityUtil.Property id = EntityUtil.getIdProperty(entityClazz);
            String sql = "DELETE FROM " + table + " WHERE " + id.getColumn() + "=?";

            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setObject(1, id.getGetter().invoke(obj));
            count += stmt.executeUpdate();
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

    public int delete(String table, List<T> lstObj) {
        if (lstObj.size() == 0) {
            return 0;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        int count = 0, noInfo = 0, failed = 0;

        try {
            EntityUtil.Property id = EntityUtil.getIdProperty(entityClazz);

            conn = dataSource.getConnection();

            /*int leave = lstObj.size();
            int idx = 0;

            for (int i = 0; i < BATCH_SIZE.length; i++) {
                if (leave >= BATCH_SIZE[i]) {
                    try {
                        String sql = "DELETE FROM " + table + " WHERE "
                                + replicateWhere(id.getColumn() + "=?", BATCH_SIZE[i]);

                        stmt = conn.prepareStatement(sql);

                        while (leave >= BATCH_SIZE[i]) {
                            for (int j = 0; j < BATCH_SIZE[i]; j++) {
                                T obj = lstObj.get(idx++);
                                stmt.setObject(1, id.getGetter().invoke(obj));
                            }

                            try {
                                count += stmt.executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            leave -= BATCH_SIZE[i];
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (stmt != null) {
                                stmt.close();
                                stmt = null;
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }*/

            String sql = "DELETE FROM " + table + " WHERE " + id.getColumn() + "=?";
            stmt = conn.prepareStatement(sql);

            int begin = 0, end = lstObj.size();
            while (begin < end) {
                for (int i = begin; i < end; i++) {
                    try {
                        T obj = lstObj.get(i);
                        stmt.setObject(1, id.getGetter().invoke(obj));
                        stmt.addBatch();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                // 批量执行
                try {
                    for (int n : stmt.executeBatch()) {
                        if (n == Statement.SUCCESS_NO_INFO) {
                            noInfo++;
                        } else if (n == Statement.EXECUTE_FAILED) {
                            failed++;
                        } else {
                            count += n;
                        }
                    }
                    break;
                } catch (BatchUpdateException e) {
                    e.printStackTrace();

                    int[] rs = e.getUpdateCounts();
                    for (int n : rs) {
                        if (n == Statement.SUCCESS_NO_INFO) {
                            noInfo++;
                        } else if (n == Statement.EXECUTE_FAILED) {
                            failed++;
                        } else {
                            count += n;
                        }
                    }
                    begin += rs.length;
                    stmt.clearBatch();
                }
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

        if (noInfo > 0 || failed > 0) {
            logger.warn("encounter error when excuteBatch. noInfo: {}, failed: {}.", noInfo, failed);
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
                    for (int i = 1; i <= columnCount; i++) {
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
