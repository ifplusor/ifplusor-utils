package psn.ifplusor.dao.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import psn.ifplusor.core.utils.ErrorUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HibernateDaoImpl implements HibernateDao {

    private static final Logger logger = LoggerFactory.getLogger(HibernateDaoImpl.class);

    // 使用线程安全的集合对象
    private final List<Object> lstInserts = Collections.synchronizedList(new ArrayList<Object>());
    private final List<Object> lstInserts2 = Collections.synchronizedList(new ArrayList<Object>());
    private final List<Object> lstUpdates = Collections.synchronizedList(new ArrayList<Object>());
    private final List<Object> lstUpdates2 = Collections.synchronizedList(new ArrayList<Object>());

    private int nUpdate = 200;
    private int nInsert = 100;

    private String daoKey;

    public HibernateDaoImpl(String key) {
        daoKey = key;
    }

    public boolean insertOne(Object o) {
        Session session = HibernateSessionFactory.getSession(daoKey);
        try {
            Transaction trans = session.beginTransaction();
            trans.setTimeout(30000);
            session.saveOrUpdate(o);
            trans.commit();
            return true;
        } catch (Exception e) {
            logger.error(ErrorUtil.getStackTrace(e));
        } finally {
            session.clear();
            try {
                session.close();
            } catch (HibernateException e) {
                logger.error(ErrorUtil.getStackTrace(e));
            }
        }
        return false;
    }

    public boolean insert(Object o, boolean useCache) {
        if (o == null) return false;
        if (!useCache) return insertOne(o);

        synchronized (lstInserts) {
            lstInserts.add(o);
            if (lstInserts.size() > nInsert) {
                // insert
                Session session = HibernateSessionFactory.getSession(daoKey);
                try {
                    Transaction trans = session.beginTransaction();
                    trans.setTimeout(30000);

                    for (Object o2 : lstInserts) {
                        session.saveOrUpdate(o2);
                    }

                    logger.debug("start commit, count: " + lstInserts.size());
                    trans.commit();
                    logger.debug("commit end.");

                    lstInserts.clear();
                    return true;
                } catch (Exception e) {
                    logger.error("encounter exception when commit, try insert again one by one.");
                    session.clear();
                    session.close();
                    for (Object o2 : lstInserts) {
                        insertOne(o2);
                    }
                } finally {
                    try {
                        session.clear();
                        session.close();
                    } catch (Exception e) {
                        logger.error(ErrorUtil.getStackTrace(e));
                    }
                }
            }
        }

        return true;
    }

    public boolean updateOne(Object o) {
        Session session = HibernateSessionFactory.getSession(daoKey);
        try {
            Transaction trans = session.beginTransaction();
            trans.setTimeout(30000);
            session.update(o);
            trans.commit();
            return true;
        } catch (Exception e) {
            logger.error(ErrorUtil.getStackTrace(e));
        } finally {
            session.clear();
            try {
                session.close();
            } catch (HibernateException e) {
                logger.error(ErrorUtil.getStackTrace(e));
            }
        }
        return false;
    }

    public boolean update(Object o, boolean useCache) {
        if (o == null) return false;
        if (!useCache) return updateOne(o);

        synchronized (lstUpdates) {
            lstUpdates.add(o);
            if (lstUpdates.size() > nUpdate) {
                // insert
                Session session = HibernateSessionFactory.getSession(daoKey);
                try {
                    Transaction trans = session.beginTransaction();
                    trans.setTimeout(30000);

                    for (Object o2 : lstInserts) {
                        session.update(o2);
                    }

                    logger.debug("start commit, count: " + lstUpdates.size());
                    trans.commit();
                    logger.debug("commit end.");

                    lstUpdates.clear();
                    return true;
                } catch (Exception e) {
                    logger.error("encounter exception when commit, try update again one by one.");
                    session.clear();
                    session.close();
                    for (Object o2 : lstUpdates) {
                        updateOne(o2);
                    }
                } finally {
                    try {
                        session.clear();
                        session.close();
                    } catch (Exception e) {
                        logger.error(ErrorUtil.getStackTrace(e));
                    }
                }
            }
        }

        return true;
    }

    public boolean delete(Object o) {
        Session session = HibernateSessionFactory.getSession(daoKey);
        try {
            Transaction trans = session.beginTransaction();
            try {
                session.delete(o);
                trans.commit();
                return true;
            } catch (Exception e) {
                logger.error("encounter exception when commit, rollback!");
                trans.rollback();
            }
        } catch (Exception e) {
            logger.error(ErrorUtil.getStackTrace(e));
        } finally {
            session.clear();
            session.close();
        }
        return false;
    }

    public int delete(String hql) {
        Session session = HibernateSessionFactory.getSession(daoKey);
        try {
            Query query = session.createQuery(hql);
            int ref = query.executeUpdate();
            session.beginTransaction().commit();
            return ref;
        } catch (Exception e) {
            logger.error(ErrorUtil.getStackTrace(e));
            return 0;
        } finally {
            session.clear();
            session.close();
        }
    }

    public List SearchList(String hsql) {
        return SearchListByLimit(hsql, 0, 0);
    }

    public List SearchListByLimit(String hsql, int nBegin, int nCount) {
        List list = null;
        Session session = HibernateSessionFactory.getSession(daoKey);
        try {

            Query query = session.createQuery(hsql);

            if (nBegin != 0 || nCount != 0) {
                query.setFirstResult(nBegin);
                query.setMaxResults(nCount);
            }

            Transaction trans = session.beginTransaction();
            list = query.list();
            trans.commit();

        } catch (Exception e) {

            if (e.getMessage().contains("#connect")) {
                logger.error("断线2，67试图恢复重连");
                try {
                    Thread.sleep(1000 * 60 * 20);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            if (e.getMessage().contains("could not extract ResultSet")) {
                logger.error("断线，67试图恢复重连");
                try {
                    Thread.sleep(1000 * 60 * 20);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }

            System.out.println("107 search Error:" + e.getMessage());
        } finally {
            session.clear();
            session.close();
        }
        return list;
    }

    public Object SearchListOne(String hsql) {
        Session session = HibernateSessionFactory.getSession(daoKey);

        try {
            Transaction trans = session.beginTransaction();
            List list = session.createQuery(hsql).list();
            trans.commit();

            if (list.size() > 0) {
                return list.get(0);
            }
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            session.clear();
            session.close();
        }

        return null;
    }

    public Integer SearchListCount(String hsql) {
        int nCount = 0;
        Session session = HibernateSessionFactory.getSession(daoKey);
        try {
            Transaction trans = session.beginTransaction();
            nCount = ((Number) session.createQuery(hsql).uniqueResult()).intValue();
            trans.commit();
        } catch (Exception e) {
            System.out.println("107 count Error:" + e.getMessage());
        } finally {
            session.clear();
            session.close();
        }
        return nCount;

    }

}
