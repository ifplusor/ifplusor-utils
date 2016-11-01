package psn.ifplusor.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * {link javax.persistence.Column}只支持注解在属性上
 *
 * @author james
 * @version 10/12/16
 */
public class EntityUtil {

    // 持久化实体类属性
    private static Map<String, List<Property>> htClassProperties
            = new HashMap<String, List<Property>>();

    // 持久化实体类主键
    private static Map<String, Property> htClassIdProperty = new HashMap<String, Property>();

    public static void clear() {
        Map<String, List<Property>> htBackup1 = htClassProperties;
        try {
            htClassProperties = new HashMap<String, List<Property>>();
            htBackup1.clear();
        } catch (OutOfMemoryError e) {
            htClassProperties.clear();
        }

        Map<String, Property> htBackup2 = htClassIdProperty;
        try {
            htClassIdProperty = new HashMap<String, Property>();
            htBackup2.clear();
        } catch (OutOfMemoryError e) {
            htClassIdProperty.clear();
        }
    }

    public static class Property {
        String column = null;
        Field field = null;
        Method getter = null;
        Method setter = null;

        Property(String column, Field field, Method getter, Method setter) {
            this.column = column;
            this.field = field;
            this.getter = getter;
            this.setter = setter;
        }

        public String getColumn() {
            return column;
        }

        public Field getField() {
            return field;
        }

        public Method getGetter() {
            return getter;
        }

        public Method getSetter() {
            return setter;
        }
    }

    private static Method getterMethod(Class<?> clazz, Field field) {

        String name = field.getName();
        String getterName = "get" + ((name.charAt(0) >= 'a' && name.charAt(0) <= 'z')
                ? (char) (name.charAt(0) - 32) + name.substring(1, name.length()) : name);

        try {
            return clazz.getDeclaredMethod(getterName);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static Method setterMethod(Class<?> clazz, Field field) {

        String name = field.getName();
        String setterName = "set" + ((name.charAt(0) >= 'a' && name.charAt(0) <= 'z')
                ? (char) (name.charAt(0) - 32) + name.substring(1, name.length()) : name);

        try {
            return clazz.getDeclaredMethod(setterName, field.getType());
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static List<Property> getColumnProperties(Class<?> clazz) {

        if (htClassProperties.containsKey(clazz.getName())) {
            return htClassProperties.get(clazz.getName());
        }

        Set<String> setColumn = new HashSet<String>();
        List<Property> lstProperties = new ArrayList<Property>();

        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            Column column = field.getDeclaredAnnotation(Column.class);
            if (column == null) continue;
            if (setColumn.contains(column.name())) {
                throw new RuntimeException("\"@Column\" of \"" + column.name() + "\" is replicate!");
            }
            Property property = new Property(column.name(), field,
                    getterMethod(clazz, field), setterMethod(clazz, field));
            lstProperties.add(property);
        }

        htClassProperties.put(clazz.getName(), lstProperties);

        return lstProperties;
    }

    public static Property getIdProperty(Class<?> clazz) throws Exception {

        if (htClassIdProperty.containsKey(clazz.getName())) {
            return htClassIdProperty.get(clazz.getName());
        }

        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            Id id = field.getDeclaredAnnotation(Id.class);
            if (id == null) continue;
            Column column = field.getDeclaredAnnotation(Column.class);
            if (column == null) {
                throw new Exception("The Id could not annotate non-Column field!");
            }

            Property property = new Property(column.name(), field,
                    getterMethod(clazz, field), setterMethod(clazz, field));
            htClassIdProperty.put(clazz.getName(), property);

            return property;
        }

        throw new Exception("Can not find Id field!");
    }

    public static <T> String genUpdateSql(String table, String where, Set<String> exColumn, T obj, Class<T> clazz)
            throws SQLException {

        if (table == null || table.trim().length() == 0) {
            throw new SQLException("Table could not null or empty!");
        }

        List<Property> lstProperties = getColumnProperties(clazz);

        StringBuilder sql  = new StringBuilder();
        sql.append("UPDATE ").append(table).append(" SET ");

        try {
            for (Property property : lstProperties) {
                if (exColumn.contains(property.column)) continue;
                // 简单的拼接字符串
                sql.append(property.column).append("='").append(property.getter.invoke(obj).toString()).append("',");
            }
        } catch (ReflectiveOperationException e) {
            throw new SQLException("Encounter error when build update sql!\n" + e.getMessage());
        }
        sql.deleteCharAt(sql.length() - 1);

        if (where != null && where.trim().length() != 0) {
            sql.append(" WHERE ").append(where);
        }

        return sql.toString();
    }

    public static <T> String genInsertSql(String table, T obj, Class<T> clazz)
            throws SQLException {

        if (table == null || table.trim().length() == 0) {
            throw new SQLException("Table could not null or empty!");
        }

        List<Property> lstProperties = getColumnProperties(clazz);

        StringBuilder sql  = new StringBuilder();
        sql.append("INSERT INTO ").append(table).append(" (");

        StringBuilder values = new StringBuilder();
        values.append("VALUES (");

        try {
            for (Property property : lstProperties) {
                // 简单的拼接字符串
                sql.append(property.column).append(",");
                values.append("'").append(property.getter.invoke(obj).toString()).append("',");
            }
        } catch (ReflectiveOperationException e) {
            throw new SQLException("Encounter error when build update sql!\n" + e.getMessage());
        }
        values.deleteCharAt(values.length() - 1);
        values.append(")");

        sql.deleteCharAt(sql.length() - 1);
        sql.append(") ").append(values);

        return sql.toString();
    }

    private static Map<String, Field> getColumnFields(Class<?> clazz) {

        Map<String, Field> htColumnToField = new HashMap<String, Field>();

        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            Column column = field.getDeclaredAnnotation(Column.class);
            if (column == null) continue;
            if (htColumnToField.containsKey(column.name())) {
                throw new RuntimeException("\"@Column\" of \"" + column.name() + "\" is replicate!");
            }
            htColumnToField.put(column.name(), field);
        }

        Method[] methods =  clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getModifiers() % 2 == 0) continue; // public
            String name = method.getName();
            Class<?> params[] = method.getParameterTypes();
            if (name.startsWith("get") && name.length() > 3 && params.length == 0
                    && (name.charAt(3) < 'a' || name.charAt(3) > 'z')) {
                // getter method
                System.out.println(name + "()");
            }

            if (name.startsWith("set") && name.length() > 3 && params.length == 1
                    && (name.charAt(3) < 'a' || name.charAt(3) > 'z')) {
                // setter method
                System.out.println(name + "(" + params[0] + ")");
            }
        }

        return htColumnToField;
    }

    public static <T> T beanFromResultSet(ResultSet rs, Class<T> clazz) throws IllegalAccessException, InstantiationException {
        if (rs == null || clazz == null)
            throw new IllegalArgumentException();

        T obj = clazz.newInstance();

        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            String name = field.getName();
            Class<?> type = field.getType();
            Column column = field.getAnnotation(Column.class);

            if (column == null) continue;

            String fieldName = column.name();

            try {
                field.setAccessible(true);

                if (type == String.class) {
                    String value = rs.getString(fieldName);
                    field.set(obj, value == null ? "" : value);
                } else if (type == int.class || type == Integer.class) {
                    field.set(obj, rs.getInt(fieldName));
                } else if (type == long.class || type == Long.class) {
                    field.set(obj, rs.getLong(fieldName));
                } else if (type == double.class || type == Double.class) {
                    field.set(obj, rs.getDouble(fieldName));
                } else if (type == float.class || type == Float.class) {
                    field.set(obj, rs.getFloat(fieldName));
                } else if (type == boolean.class || type == Boolean.class) {
                    field.set(obj, rs.getBoolean(fieldName));
                } else {
                    Object value = rs.getObject(fieldName);
                    if (value != null)
                        field.set(obj, rs.getDate(fieldName));
                }
            } catch (SQLException e) {
                if (type == String.class) {
                    field.set(obj, "");
                } else if (type == int.class || type == Integer.class) {
                    field.set(obj, 0);
                } else if (type == long.class || type == Long.class) {
                    field.set(obj, 0L);
                } else if (type == double.class || type == Double.class) {
                    field.set(obj, 0.0);
                } else if (type == float.class || type == Float.class) {
                    field.set(obj, 0.0);
                } else if (type == boolean.class || type == Boolean.class) {
                    field.set(obj, false);
                } else {
                    field.set(obj, new Object());
                }
            }
        }

        return obj;
    }
}
