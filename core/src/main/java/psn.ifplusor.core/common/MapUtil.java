package psn.ifplusor.core.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author james
 * @version 10/20/16
 */
public class MapUtil {

    private static int capacity = 10;

    public static void setInitialCapacity(int capacity) {
        MapUtil.capacity = capacity;
    }

    public static <K, V> int addToListMap(Map<K, List<V>> map, K key, V value, Class<? extends List> clazz)
            throws IllegalAccessException, InstantiationException {

        if (key == null || value == null) {
            return -1;
        }

        if (!map.containsKey(key)) {
            List<V> list = null;
            if (clazz.equals(ArrayList.class)) {
                list = new ArrayList<V>(capacity);
            } else {
                list = clazz.newInstance();
            }
            list.add(value);
            map.put(key, list);
            return 1;
        } else {
            List<V> lst = map.get(key);
            lst.add(value);
            return lst.size();
        }
    }

    public static <K, V> int addToListMap(Map<K, List<V>> map, K key, V value) {
        try {
            return addToListMap(map, key, value, ArrayList.class);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static <K> void addToCountMap(Map<K, Integer> map, K key, Integer count) {

        if (key == null) {
            return;
        }

        if (!map.containsKey(key)) {
            map.put(key, count);
        } else {
            Integer oldCount = map.get(key);
            if (oldCount == null) {
                map.put(key, count);
            } else if (count == null) {
                map.put(key, null);
            } else {
                map.put(key, oldCount + count);
            }
        }
    }

    public static <K> void addToCountMap(Map<K, Integer> map, K key) {
        addToCountMap(map, key, 1);
    }
}
