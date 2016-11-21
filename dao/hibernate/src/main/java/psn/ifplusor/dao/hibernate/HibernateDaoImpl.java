package psn.ifplusor.dao.hibernate;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HibernateDaoImpl implements HibernateDao {

    static List<Object> lstInserts = Collections
            .synchronizedList(new ArrayList<Object>());
    static List<Object> lstInserts2 = Collections
            .synchronizedList(new ArrayList<Object>());
    static List<Object> lstUpdates = Collections
            .synchronizedList(new ArrayList<Object>());
    static List<Object> lstUpdates2 = Collections
            .synchronizedList(new ArrayList<Object>());

    public static final Logger logger = LoggerFactory.getLogger(HibernateDaoImpl.class);

    private String daoKey;

    public HibernateDaoImpl(String key) {
        daoKey = key;
    }

    int nUpdate = 200;
    int nInsert = 100;

    public Boolean insert(Object o, String id, Boolean bSave) {

        if (o == null) {
            return false;
        }

        ArrayList<Object> lstTempInserts = new ArrayList<Object>();
        synchronized (lstInserts) {
            if (!bSave) {
                if (lstInserts.size() < nInsert) {
                    if (o == null) {
                        return true;
                    }
                    lstInserts.add(o);

                    return true;
                } else {
                    lstInserts2.add(o);
                }
            } else {

                if (o != null) {
                    lstInserts.add(o);
                }
            }

            lstTempInserts.addAll(lstInserts);

            lstInserts.clear();
            lstInserts.addAll(lstInserts2);
            lstInserts2.clear();
        }

        Session session = HibernateSessionFactory.getSession(daoKey);

        Transaction trans = session.beginTransaction();
        trans.setTimeout(30000);

        try {

            for (Object o2 : lstTempInserts) {

                session.saveOrUpdate(o2);
            }

            logger.info("!!!!!!!!!!!!!!!!Begin Insert Commit 107 Count:  lstInserts:"
                    + lstTempInserts.size());
            trans.commit();
            logger.info("!!!!!!!!!!!!!!!!End Insert Commit 107 Count:  lstInserts:"
                    + lstTempInserts.size());

            return true;
        } catch (Exception e) {
            logger.error("~~~~~~~~~~~~~~~~~~~~~~~~~~~107 insert Error commit:"
                    + e.getMessage());
            logger.error("lstTempInserts:" + lstTempInserts.size());
            session.clear();
            session.close();
//            HibernateSessionFactory.closeSession(daoKey);

            for (Object o2 : lstTempInserts) {
                try {
                    session = HibernateSessionFactory.getSession(daoKey);
                    trans = session.beginTransaction();

                    session.saveOrUpdate(o2);
                    trans.commit();

                } catch (Exception e2) {
                    // if(!e2.getMessage().contains("Duplicate entry"))
                    {
                        logger.error("107 sec error Input:" + e2.getMessage());
                    }
                } finally {
                    session.clear();
                    session.close();
//                    HibernateSessionFactory.closeSession(daoKey);
                    // logger.info("107 sec Input:"+id);
                }
            }
        } finally {
            try {
                session.clear();
                session.close();
//                HibernateSessionFactory.closeSession(daoKey);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

        return true;
    }

    public Boolean update(Object o, String id, Boolean bSave) {
        try {
            if (o == null) {
                return false;
            }
            ArrayList<Object> lstTempUpdates = new ArrayList<Object>();
            synchronized (lstUpdates) {
                if (!bSave) {
                    if (lstUpdates.size() < nUpdate) {
                        lstUpdates.add(o);
                        return true;
                    } else {
                        lstUpdates2.add(o);
                    }
                } else {

                    if (o != null) {
                        lstUpdates.add(o);
                    }
                }

                lstTempUpdates.addAll(lstUpdates);

                lstUpdates.clear();
                lstUpdates.addAll(lstUpdates2);
                lstUpdates2.clear();

            }

            Session session = HibernateSessionFactory.getSession(daoKey);

            Transaction trans = session.beginTransaction();
            trans.setTimeout(30000);

            session.flush();

            try {

                for (Object o2 : lstTempUpdates) {
                    if (o2 == null) {
                        continue;
                    }
                    session.saveOrUpdate(o2);
                }

                trans.commit();

                return true;

            } catch (Exception e) {
                logger.error("107 Update Error commit:" + e.getMessage());
                session.clear();
                session.close();
//                HibernateSessionFactory.closeSession(daoKey);

                for (Object o2 : lstTempUpdates) {
                    try {
                        session = HibernateSessionFactory.getSession(daoKey);
                        trans = session.beginTransaction();

                        session.merge(o2);
                        trans.commit();

                        // logger.info("107 sec Update");

                    } catch (Exception e2) {
                        logger.error("107 sec error Update:" + e2.getMessage());
                    } finally {
                        session.clear();
                        session.close();
//                        HibernateSessionFactory.closeSession(daoKey);
                    }
                }
            } finally {
                try {
                    session.clear();
                    session.close();
//                    HibernateSessionFactory.closeSession(daoKey);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }

        } catch (Exception e) {
            System.out.println("107 update Error:" + e.getMessage());
        }
        return false;
    }

    public Boolean updateOne(Object o) {
        try {

            Session session = HibernateSessionFactory.getSession(daoKey);

            Transaction trans = session.beginTransaction();

            try {

                session.update(o);

                trans.commit();

                return true;

            } catch (Exception e) {
                System.out.println("107 Update One Error commit:"
                        + e.getMessage());
            } finally {
                session.clear();
                session.close();
//                HibernateSessionFactory.closeSession(daoKey);
            }

        } catch (Exception e) {
            System.out.println("107 update Error:" + e.getMessage());
        }
        return false;
    }

    public Boolean delete(Object o) {

        Session session = HibernateSessionFactory.getSession(daoKey);
        try {

            Transaction trans = session.beginTransaction();

            try {
                session.delete(o);

                trans.commit();
            } catch (Exception e) {
                System.out.println("107 delete Error commit:" + e.getMessage());
                trans.rollback();
            } finally {
                session.clear();
                session.close();
//                HibernateSessionFactory.closeSession(daoKey);
            }

            return true;
        } catch (Exception e) {
            System.out.println("107 del Error:" + e.getMessage());
        }

        return false;
    }

    public int delete(String hsql) {
        int ref = 0;
        Session session = HibernateSessionFactory.getSession(daoKey);
        try {

            String hql = hsql;

            Query query = session.createQuery(hql);

            ref = query.executeUpdate();

            session.beginTransaction().commit();

            System.out.println("delete dates=>" + ref);
        } catch (Exception e) {
            System.out.println("107 del Error:" + e.getMessage());
        } finally {
            session.clear();
            session.close();
//            HibernateSessionFactory.closeSession(daoKey);
        }

        return ref;
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
//            HibernateSessionFactory.closeSession(daoKey);
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
//            HibernateSessionFactory.closeSession(daoKey);
        }

        return null;
    }

    public Integer SearchListCount(String hsql) {
        int nCount = 0;
        Session session = HibernateSessionFactory.getSession(daoKey);
        try {

            Transaction trans = session.beginTransaction();

            nCount = ((Number) session.createQuery(hsql).uniqueResult())
                    .intValue();

            trans.commit();

        } catch (Exception e) {
            System.out.println("107 count Error:" + e.getMessage());
        } finally {
            session.clear();
            session.close();
//            HibernateSessionFactory.closeSession(daoKey);
        }
        return nCount;

    }

}
