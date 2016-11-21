package psn.ifplusor.dao.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

public class RedisDaoImpl implements RedisDao {

    private String daoKey;

    public RedisDaoImpl(String key) {
        daoKey = key;
    }

    public String get(int index, String key) {
        boolean broken = false;
        Jedis jedis = null;
        try {
            jedis = JedisFactory.getJedis(daoKey);
            String status = jedis.select(index);
            return jedis.get(key);
        } catch (JedisException e) {
            broken = JedisFactory.isBroken(e);
            e.printStackTrace();
            return null;
        } finally {
            JedisFactory.returnJedis(daoKey, jedis, broken);
        }
    }

    public String pop(int index, String key) {
        boolean broken = false;
        String status = null;
        Jedis jedis = null;
        try {
            jedis = JedisFactory.getJedis(daoKey);
            status = jedis.select(index);
            return jedis.lpop(key);
        } catch (JedisException e) {
            broken = JedisFactory.isBroken(e);
            e.printStackTrace();
            return null;
        } finally {
            JedisFactory.returnJedis(daoKey, jedis, broken);
        }
    }

    public void push(int index, String key, String value) {
        boolean broken = false;
        String status = null;
        Jedis jedis = null;
        try {
            jedis = JedisFactory.getJedis(daoKey);
            status = jedis.select(index);
            jedis.lpush(key, value);
        } catch (JedisException e) {
            broken = JedisFactory.isBroken(e);
            e.printStackTrace();
        } finally {
            JedisFactory.returnJedis(daoKey, jedis, broken);
        }
    }

    public Boolean exist(int index, String key) {
        boolean broken = false;
        String status = null;
        Jedis jedis = null;
        try {
            jedis = JedisFactory.getJedis(daoKey);
            status = jedis.select(index);
            return jedis.exists(key);
        } catch (JedisException e) {
            broken = JedisFactory.isBroken(e);
            e.printStackTrace();
            return null;
        } finally {
            JedisFactory.returnJedis(daoKey, jedis, broken);
        }
    }

    public Boolean flush(int index) {
        boolean broken = false;
        String status = null;
        Jedis jedis = null;
        try {
            jedis = JedisFactory.getJedis(daoKey);
            status = jedis.select(index);
            status = jedis.flushDB();
            return true;
        } catch (JedisException e) {
            broken = JedisFactory.isBroken(e);
            e.printStackTrace();
            return null;
        } finally {
            JedisFactory.returnJedis(daoKey, jedis, broken);
        }
    }

    public Long size() {
        boolean broken = false;
        Jedis jedis = null;
        try {
            jedis = JedisFactory.getJedis(daoKey);
            return jedis.dbSize();
        } catch (JedisException e) {
            broken = JedisFactory.isBroken(e);
            e.printStackTrace();
            return null;
        } finally {
            JedisFactory.returnJedis(daoKey, jedis, broken);
        }
    }

    public long llen(int index, String key) {
        boolean broken = false;
        String status = null;
        Jedis jedis = null;
        try {
            jedis = JedisFactory.getJedis(daoKey);
            String code = jedis.select(index);
            return jedis.llen(key);
        } catch (JedisException e) {
            broken = JedisFactory.isBroken(e);
            e.printStackTrace();
            return 0;
        } finally {
            JedisFactory.returnJedis(daoKey, jedis, broken);
        }
    }
}
