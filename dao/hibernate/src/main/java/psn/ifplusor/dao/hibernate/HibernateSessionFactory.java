package psn.ifplusor.dao.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.io.File;
import java.util.HashMap;

/**
 * Configures and provides access to Hibernate sessions, tied to the
 * current thread of execution.  Follows the Thread Local Session
 * pattern, see <a href="http://hibernate.org/42.html">http://hibernate.org/42.html</a>.
 */
public class HibernateSessionFactory {

    /**
     * Location of hibernate.cfg.xml file.
     * Location should be on the classpath as Hibernate uses
     * #resourceAsStream style lookup for its configuration file.
     * The default classpath location of the hibernate config file is
     * in the default package. Use #setConfigFile() to update
     * the location of the configuration file for the current session.
     */
    private static final HashMap<String, SessionFactory> htSessionFactory = new HashMap<String, SessionFactory>();
    private static final HashMap<String, String> htConfigFilePath = new HashMap<String, String>();
    private static final HashMap<String, Session> htSession = new HashMap<String, Session>();

    private HibernateSessionFactory() {
    }

    /**
     * Register hibernate session factory.
     *
     * @param key  the key for get session using this factory.
     * @param path the configure file path for build factory.
     */
    public static void registerSessionFactory(String key, String path) {
        if (key == null || "".equals(key.trim()) || path == null || "".equals(path.trim())) {
            throw new IllegalArgumentException("Invalid Argument");
        }
        File configFile = new File(path);
        if (!configFile.exists()) {
            throw new HibernateException("Configure file is not exist. path: " + path);
        }
        synchronized (htSessionFactory) {
            htConfigFilePath.put(key, path);
            SessionFactory sessionFactory = htSessionFactory.remove(key);
            if (sessionFactory != null && sessionFactory.isClosed()) {
                sessionFactory.close();
            }
        }
    }

    /**
     * Returns the ThreadLocal Session instance.  Lazy initialize
     * the <code>SessionFactory</code> if needed.
     *
     * @return Session
     */
    public static Session getSession(String key) {
        SessionFactory sessionFactory = htSessionFactory.get(key);
        if (sessionFactory == null || sessionFactory.isClosed()) {
            synchronized (htSessionFactory) {
                sessionFactory = htSessionFactory.get(key);
                if (sessionFactory == null || sessionFactory.isClosed()) {
                    sessionFactory = rebuildSessionFactory(key);
                    SessionFactory t = htSessionFactory.put(key, sessionFactory);
                    if (t != null && !t.isClosed()) {
                        t.close();
                    }
                }
            }
        }
        return sessionFactory.openSession();
    }

    /**
     * Rebuild hibernate session factory
     *
     * @throws IllegalArgumentException - 配置文件不存在
     */
    private static SessionFactory rebuildSessionFactory(String key) {
        String configFilePath = htConfigFilePath.get(key);
        if (configFilePath == null) {
            throw new IllegalArgumentException("Can't find config for \"" + key + "\".");
        }

        Configuration configuration = new Configuration().configure(new File(configFilePath));
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(
                configuration.getProperties()).build();
        return configuration.buildSessionFactory(serviceRegistry);
    }

    /**
     * Release all hibernate session factory instance.
     */
    public static void releaseAllSessionFactory() {
        synchronized (htSessionFactory) {
            for (String key : htSessionFactory.keySet()) {
                SessionFactory sessionFactory = htSessionFactory.get(key);
                if (sessionFactory != null && !sessionFactory.isClosed()) {
                    sessionFactory.close();
                }
            }
            htSessionFactory.clear();
        }
    }
}