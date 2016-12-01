package psn.ifplusor.dao.mongodb;

import com.mongodb.MongoClient;

import java.util.HashMap;

/**
 * @author james
 * @version 11/30/16
 */
public class MongoClientFactory {

    private static class MongoConfig {
        String host;
        int port;

        MongoConfig(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }

    private static final HashMap<String, MongoClient> htMongoClient = new HashMap<String, MongoClient>();
    private static final HashMap<String, MongoConfig> htMongoConfig = new HashMap<String, MongoConfig>();

    private MongoClientFactory() { }

    public static void registerMongoClient(String key, String host) {
        registerMongoClient(key, host, 27017);
    }

    public static void registerMongoClient(String key, String host, int port) {
        MongoConfig jedisConfig = new MongoConfig(host, port);

        synchronized (htMongoConfig) {
            htMongoConfig.put(key, jedisConfig);
            MongoClient client = htMongoClient.remove(key);
            if (client != null) {
                client.close();
            }
        }
    }

    private static MongoClient rebuildMongoClient(String key) {
        MongoConfig mongoConfig = htMongoConfig.get(key);
        if (mongoConfig == null) {
            throw new IllegalArgumentException("Can't find config for \"" + key + "\".");
        }

        return new MongoClient(mongoConfig.host, mongoConfig.port);
    }

    public static MongoClient getMongoClient(String key) {
        MongoClient mongoClient = htMongoClient.get(key);
        if (mongoClient == null) {
            synchronized (htMongoClient) {
                mongoClient = htMongoClient.get(key);
                if (mongoClient == null) {
                    mongoClient = rebuildMongoClient(key);
                    htMongoClient.put(key, mongoClient);
                }
            }
        }
        return mongoClient;
    }

    public static void releaseAllMongoClient() {
        synchronized (htMongoClient) {
            for (String key : htMongoClient.keySet()) {
                MongoClient mongoClient = htMongoClient.get(key);
                if (mongoClient != null) {
                    mongoClient.close();
                }
            }
            htMongoClient.clear();
        }
    }
}
