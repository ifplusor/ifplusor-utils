package psn.ifplusor.dao.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import psn.ifplusor.core.utils.ErrorUtil;
import psn.ifplusor.core.utils.UUIDGenerator;
import redis.clients.jedis.Jedis;

/**
 * @author James Yin
 * @version 10/27/17
 */
public class RedisLock {

    private static final Logger logger = LoggerFactory.getLogger(RedisLock.class);

    private static String lockKeyPrefix = "_lock:";
    private static String hostId = UUIDGenerator.getUUIDSmall();

    public static void setLockKeyPrefix(String prefix) {
        lockKeyPrefix = prefix;
    }

    /**
     * @param acquireTimeout
     * @param lockTimeout
     * @return lock id
     */
    public static String lockWithTimeout(Jedis jedis, String lockName, int acquireTimeout, int lockTimeout) {
        String lockKey = lockKeyPrefix + lockName;
        String identifier = hostId;
        logger.trace("try lock use key:" + lockKey + ", value: " + identifier);
        long end = System.currentTimeMillis() + acquireTimeout * 1000L;
        while (System.currentTimeMillis() < end) {
            String ret = jedis.set(lockKey, identifier, "NX", "EX", lockTimeout);
            if ("OK".equals(ret)) {
                return identifier;
//            if (jedis.setnx(lockKey, identifier) == 1L) {
//                jedis.expire(lockKey, lockTimeout);
//                return identifier;
//            } else if (jedis.ttl(lockKey) == null) {
//                // 重复设置过期时间，不影响锁超时
//                jedis.expire(lockKey, lockTimeout);
            }
            try {
                // sleep or spin?
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
        }
        return null;
    }

    public static boolean unlock(Jedis jedis, String lockName, String identifier) {
        if (identifier == null) {
            return false;
        }
        String lockKey = lockKeyPrefix + lockName;
        String lockIdentifier = null;
        while (true) {
            try {
                logger.trace("try unlock use key:" + lockKey);
                lockIdentifier = jedis.get(lockKey);
                if (identifier.equals(lockIdentifier)) {
                    jedis.del(lockKey);
                    return true;
                }
                logger.trace("unlock failed! locker identifier=" + lockIdentifier);
                break;
            } catch (Exception e) {
                logger.error("error: encounter exception when unlock. \n" + ErrorUtil.getStackTrace(e));
            }
        }
        return false;
    }
}
