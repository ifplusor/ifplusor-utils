package psn.ifplusor.dao.hibernate;

import java.util.List;

public interface HibernateDao {

    boolean insert(Object o, boolean useCache);

    boolean update(Object o, boolean useCache);

    boolean delete(Object o);

    int delete(String hql);

    List SearchList(String hql);

    List SearchListByLimit(String hql, int nBegin, int nEnd);

    Object SearchListOne(String hql);

    Integer SearchListCount(String hql);
}
