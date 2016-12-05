package psn.ifplusor.core.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author james
 * @version 10/20/16
 */
public class MapUtil {

    public static <K, V> void addToListMap(Map<K, List<V>> map, K key, V value, Class<? extends List> clazz)
            throws IllegalAccessException, InstantiationException {

        if (key == null || value == null) {
            return;
        }

        if (!map.containsKey(key)) {
            List<V> list = clazz.newInstance();
            list.add(value);
            map.put(key, list);
        } else {
            List<V> lst = map.get(key);
            lst.add(value);
        }
    }

    public static <K, V> void addToListMap(Map<K, List<V>> map, K key, V value) {
        try {
            addToListMap(map, key, value, ArrayList.class);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
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
