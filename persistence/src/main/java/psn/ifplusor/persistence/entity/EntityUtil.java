package psn.ifplusor.persistence.entity;

import javax.persistence.Column;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author james
 * @version 10/12/16
 */
public class EntityUtil {

    public static Collection<String> getBeanProperties(Class clazz) {
        List<String> properties = new ArrayList<String>();

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            properties.add(field.getName());
        }

        return properties;
    }

    public static <T> T beanFromResultSet(ResultSet rs, Class<T> clazz) throws IllegalAccessException, InstantiationException {
        if (rs == null || clazz == null)
            throw new IllegalArgumentException();

        T obj = clazz.newInstance();

        Field[] fields = clazz.getDeclaredFields();

        /*Method[] methods = clazz.getDeclaredMethods();
        for(Method method : methods) {
            Column column = method.getAnnotation(Column.class);
            if (column == null) continue;
        }*/


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
                    field.set(obj, value==null?"":value);
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
