package psn.ifplusor.dao.hibernate;

import java.util.List;

public interface HibernateDao {

    Boolean insert(Object o, String id, Boolean bSave);

    Boolean update(Object o, String id, Boolean bSave);

    Boolean delete(Object o);

    int delete(String hsql);

    List SearchList(String hsql);

    List SearchListByLimit(String hsql, int nBegin, int nEnd);

    Object SearchListOne(String hsql);

    Integer SearchListCount(String hsql);
}
