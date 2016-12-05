package psn.ifplusor.dao.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.exceptions.JedisException;

import java.util.HashMap;

/**
 * @author james
 * @version 11/21/16
 */
public class JedisFactory {

    private static class JedisConfig {
        JedisPoolConfig config;
        String host;
        int port;

        JedisConfig(JedisPoolConfig config, String host, int port) {
            this.config = config;
            this.host = host;
            this.port = port;
        }
    }

    private static final HashMap<String, JedisPool> htJedisPool = new HashMap<String, JedisPool>();
    private static final HashMap<String, JedisConfig> htJedisConfig = new HashMap<String, JedisConfig>();

    private JedisFactory() { }

    public static void registerJedisPool(String key, String host) {
        registerJedisPool(key, host, 6379);
    }

    public static void registerJedisPool(String key, String host, int port) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxActive(1000);
        config.setMaxIdle(100);
        config.setTestOnBorrow(true);

        registerJedisPool(key, config, host, port);
    }

    public static void registerJedisPool(String key, JedisPoolConfig config, String host, int port) {
        JedisConfig jedisConfig = new JedisConfig(config, host, port);

        synchronized (htJedisPool) {
            htJedisConfig.put(key, jedisConfig);
            JedisPool pool = htJedisPool.remove(key);
            if (pool != null) {
                pool.destroy();
            }
        }
    }

    private static JedisPool rebuildJedisPool(String key) {
        JedisConfig jedisConfig = htJedisConfig.get(key);
        if (jedisConfig == null) {
            throw new IllegalArgumentException("Can't find config for \"" + key + "\".");
        }

        return new JedisPool(jedisConfig.config, jedisConfig.host, jedisConfig.port);
    }

    public static Jedis getJedis(String key) {
        JedisPool jedisPool = htJedisPool.get(key);
        if (jedisPool == null) {
            synchronized (htJedisPool) {
                jedisPool = htJedisPool.get(key);
                if (jedisPool == null) {
                    jedisPool = rebuildJedisPool(key);
                    htJedisPool.put(key, jedisPool);
                }
            }
        }
        return jedisPool.getResource();
    }

    public static boolean isBroken(JedisException jedisException) {
        return !(jedisException instanceof JedisDataException) ||
                (jedisException.getMessage() != null && jedisException.getMessage().contains("READONLY"));
    }

    public static boolean returnJedis(String key, Jedis jedis, boolean broken) {
        if (jedis == null) {
            return true;
        }

        JedisPool jedisPool = htJedisPool.get(key);
        if (jedisPool != null) {
            if (broken) {
                jedisPool.returnBrokenResource(jedis);
            } else {
                jedisPool.returnResource(jedis);
            }
            return true;
        }
        return false;
    }

    public static void releaseAllJedisPool() {
        synchronized (htJedisPool) {
            for (String key : htJedisPool.keySet()) {
                JedisPool jedisPool = htJedisPool.get(key);
                if (jedisPool != null) {
                    jedisPool.destroy();
                }
            }
            htJedisPool.clear();
        }
    }
}
