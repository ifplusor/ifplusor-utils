package psn.ifplusor.dao.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class RedisDaoImpl implements RedisDao {

    private static final Logger logger = LoggerFactory.getLogger(RedisDaoImpl.class);

    private final String daoKey;

    public RedisDaoImpl(String key) {
        daoKey = key;
    }

    public String get(int index, String key) {
        Jedis jedis = null;
        try {
            jedis = JedisFactory.getJedis(daoKey);
            String status = jedis.select(index);
            return jedis.get(key);
        } finally {
            if (jedis != null) jedis.close();
        }
    }

    public void set(int index, String key, String value) {
        Jedis jedis = null;
        try {
            jedis = JedisFactory.getJedis(daoKey);
            String status = jedis.set(key, value);
        } finally {
            if (jedis != null) jedis.close();
        }
    }

    public String lpop(int index, String key) {
        String status = null;
        Jedis jedis = null;
        try {
            jedis = JedisFactory.getJedis(daoKey);
            status = jedis.select(index);
            return jedis.lpop(key);
        } finally {
            if (jedis != null) jedis.close();
        }
    }

    public String rpop(int index, String key) {
        String status = null;
        Jedis jedis = null;
        try {
            jedis = JedisFactory.getJedis(daoKey);
            status = jedis.select(index);
            return jedis.rpop(key);
        } finally {
            if (jedis != null) jedis.close();
        }
    }

    public void lpush(int index, String key, String value) {
        String status = null;
        Jedis jedis = null;
        try {
            jedis = JedisFactory.getJedis(daoKey);
            status = jedis.select(index);
            jedis.lpush(key, value);
        } finally {
            if (jedis != null) jedis.close();
        }
    }

    public void rpush(int index, String key, String value) {
        String status = null;
        Jedis jedis = null;
        try {
            jedis = JedisFactory.getJedis(daoKey);
            status = jedis.select(index);
            jedis.rpush(key, value);
        } finally {
            if (jedis != null) jedis.close();
        }
    }

    public long llen(int index, String key) {
        String status = null;
        Jedis jedis = null;
        try {
            jedis = JedisFactory.getJedis(daoKey);
            String code = jedis.select(index);
            return jedis.llen(key);
        } finally {
            if (jedis != null) jedis.close();
        }
    }

    public Boolean exists(int index, String key) {
        String status = null;
        Jedis jedis = null;
        try {
            jedis = JedisFactory.getJedis(daoKey);
            status = jedis.select(index);
            return jedis.exists(key);
        } finally {
            if (jedis != null) jedis.close();
        }
    }

    public Boolean flush(int index) {
        String status = null;
        Jedis jedis = null;
        try {
            jedis = JedisFactory.getJedis(daoKey);
            status = jedis.select(index);
            status = jedis.flushDB();
            return true;
        } finally {
            if (jedis != null) jedis.close();
        }
    }

    public Long size() {
        Jedis jedis = null;
        try {
            jedis = JedisFactory.getJedis(daoKey);
            return jedis.dbSize();
        } finally {
            if (jedis != null) jedis.close();
        }
    }

    public Jedis getJedis(int index) {
        Jedis jedis = JedisFactory.getJedis(daoKey);
        String status = jedis.select(index);
        return jedis;
    }
}
