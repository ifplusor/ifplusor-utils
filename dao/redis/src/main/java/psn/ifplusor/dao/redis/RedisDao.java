package psn.ifplusor.dao.redis;

import redis.clients.jedis.Jedis;

/**
 * @author james
 * @version 11/21/16
 */
public interface RedisDao {

    // String

    String get(int index, String key);

    void set(int index, String key, String value);

    // List

    String lpop(int index, String key);

    String rpop(int index, String key);

    void lpush(int index, String key, String value);

    void rpush(int index, String key, String value);

    long llen(int index, String key);

    // Management

    Boolean exists(int index, String key);

    Boolean flush(int index);

    Long size();

    Jedis getJedis(int index);
}
