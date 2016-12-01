package psn.ifplusor.dao.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

/**
 * @author james
 * @version 11/30/16
 */
public class MongoDbDaoImpl implements MongoDbDao {

    private String daoKey;

    public MongoDbDaoImpl(String key) {
        this.daoKey = key;
    }

    public List<Document> query(String database, String collection, Bson filter) {

        MongoClient mongoClient = MongoClientFactory.getMongoClient(daoKey);

        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> dbCollection = db.getCollection(collection);

        List<Document> lst = new ArrayList<Document>();
        MongoCursor<Document> cursor = dbCollection.find(filter).iterator();

        try {
            while (cursor.hasNext()) {
                lst.add(cursor.next());
            }
        } finally {
            cursor.close();
        }

        return lst;
    }

    public Document queryOne(String database, String collection, Bson filter) {

        MongoClient mongoClient = MongoClientFactory.getMongoClient(daoKey);

        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> dbCollection = db.getCollection(collection);

        return dbCollection.find(filter).first();
    }
}
