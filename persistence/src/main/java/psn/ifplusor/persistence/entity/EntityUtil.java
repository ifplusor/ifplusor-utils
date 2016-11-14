package psn.ifplusor.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.Date;

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

	public static <T> String genUpdateSqlWithParams(String table, String where, Set<String> exColumn, Class<T> clazz)
			throws SQLException {

		if (table == null || table.trim().length() == 0) {
			throw new SQLException("Table could not null or empty!");
		}

		List<Property> lstProperties = getColumnProperties(clazz);

		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").append(table).append(" SET ");

		if (exColumn != null && exColumn.size() != 0) {
			for (Property property : lstProperties) {
				if (exColumn.contains(property.column)) continue;
				// 简单的拼接字符串
				sql.append(property.column).append("=?,");
			}
		} else {
			for (Property property : lstProperties) {
				sql.append(property.column).append("=?,");
			}
		}

		sql.deleteCharAt(sql.length() - 1);

		if (where != null && where.trim().length() != 0) {
			sql.append(" WHERE ").append(where);
		}

		return sql.toString();
	}

	public static <T> String genInsertSqlWithParams(String table, Set<String> exColumn, Class<T> clazz) throws SQLException {

		if (table == null || table.trim().length() == 0) {
			throw new SQLException("Table could not null or empty!");
		}

		List<Property> lstProperties = getColumnProperties(clazz);

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ").append(table).append(" (");

		StringBuilder values = new StringBuilder();
		values.append("VALUES (");

		if (exColumn != null && exColumn.size() != 0) {
			for (Property property : lstProperties) {
				if (exColumn.contains(property.column)) continue;
				// 简单的拼接字符串
				sql.append(property.column).append(",");
				values.append("?,");
			}
		} else {
			for (Property property : lstProperties) {
				sql.append(property.column).append(",");
				values.append("?,");
			}
		}

		values.deleteCharAt(values.length() - 1);
		values.append(")");

		sql.deleteCharAt(sql.length() - 1);
		sql.append(") ").append(values);

		return sql.toString();
	}

	public static <T> String genQuerySqlWithParams(String table, String where, Integer limit, Set<String> exColumn, Class<T> clazz) throws SQLException {

		if (table == null || table.trim().length() == 0) {
			throw new SQLException("Table could not null or empty!");
		}

		List<Property> lstProperties = getColumnProperties(clazz);

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");

		if (exColumn != null && exColumn.size() != 0) {
			for (Property property : lstProperties) {
				if (exColumn.contains(property.column)) continue;
				// 简单的拼接字符串
				sql.append(property.column).append(",");
			}
		} else {
			for (Property property : lstProperties) {
				sql.append(property.column).append(",");
			}
		}

		sql.deleteCharAt(sql.length() - 1);
		sql.append(" FROM ").append(table.trim());

		if (where != null && where.trim().length() != 0) {
			sql.append(" WHERE ").append(where);
		}

		if (limit != null && limit > 0) {
			sql.append(" LIMIT ").append(limit);
		}

		return sql.toString();
	}

	public static <T> T beanFromResultSet(ResultSet rs, Class<T> clazz)
			throws IllegalAccessException, InstantiationException {

		if (rs == null || clazz == null)
			throw new IllegalArgumentException();

		T obj = clazz.newInstance();

		List<Property> lstProperties = getColumnProperties(clazz);

		for (Property property : lstProperties) {
			String column = property.column;
			Class<?> type = property.field.getType();

			try {
				if (type == Byte.class || type == byte.class) {
					property.setter.invoke(obj, rs.getByte(column));
				} else if (type == String.class) {
					property.setter.invoke(obj, rs.getString(column));
				} else if (type == BigDecimal.class) {
					property.setter.invoke(obj, rs.getBigDecimal(column));
				} else if (type == Short.class || type == short.class) {
					property.setter.invoke(obj, rs.getShort(column));
				} else if (type == Integer.class || type == int.class) {
					property.setter.invoke(obj, rs.getInt(column));
				} else if (type == Long.class || type == long.class) {
					property.setter.invoke(obj, rs.getLong(column));
				} else if (type == Float.class || type == float.class) {
					property.setter.invoke(obj, rs.getFloat(column));
				} else if (type == Double.class || type == double.class) {
					property.setter.invoke(obj, rs.getDouble(column));
				} else if (type == byte[].class) {
					property.setter.invoke(obj, (Object) rs.getBytes(column));
				} else if (type == java.sql.Date.class) {
					property.setter.invoke(obj, rs.getDate(column));
				} else if (type == Time.class) {
					property.setter.invoke(obj, rs.getTime(column));
				} else if (type == Timestamp.class) {
					property.setter.invoke(obj, rs.getTime(column));
				} else if (type == Boolean.class || type == boolean.class) {
					property.setter.invoke(obj, rs.getBoolean(column));
				} else if (type == InputStream.class) {
					property.setter.invoke(obj, rs.getBinaryStream(column));
				} else if (type == java.sql.Blob.class) {
					property.setter.invoke(obj, rs.getBlob(column));
				} else if (type == java.sql.Clob.class) {
					property.setter.invoke(obj, rs.getClob(column));
				} /*else if (this.treatUtilDateAsTimestamp.getValue() && parameterObj instanceof java.util.Date) {
					setTimestamp(parameterIndex, new Timestamp(((java.util.Date) parameterObj).getTime()));
				}*/ else if (type == BigInteger.class) {
					property.setter.invoke(obj, new BigInteger(rs.getString(column)));
				} /*else if (type == LocalDate.class) {
					setDate(parameterIndex, java.sql.Date.valueOf((LocalDate) parameterObj));
				} else if (parameterObj instanceof LocalDateTime) {
					setTimestamp(parameterIndex, Timestamp.valueOf((LocalDateTime) parameterObj));
				} else if (parameterObj instanceof LocalTime) {
					setTime(parameterIndex, Time.valueOf((LocalTime) parameterObj));
				}*/ else {
					property.setter.invoke(obj, rs.getString(column));
//					setSerializableObject(parameterIndex, parameterObj);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		return obj;
	}
}
