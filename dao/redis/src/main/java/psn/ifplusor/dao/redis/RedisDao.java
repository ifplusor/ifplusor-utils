package psn.ifplusor.dao.redis;

/**
 * @author james
 * @version 11/21/16
 */
public interface RedisDao {

    String get(int index, String key);

    String pop(int index, String key);

    void push(int index, String key, String value);

    Boolean exist(int index, String key);

    Boolean flush(int index);

    Long size();

    long llen(int index, String key);
}
