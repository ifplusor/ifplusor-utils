package psn.ifplusor.dao.mongodb;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

/**
 * @author james
 * @version 11/30/16
 */
public interface MongoDbDao {

    List<Document> query(String database, String collection, Bson filter);

    Document queryOne(String database, String collection, Bson filter);
}
