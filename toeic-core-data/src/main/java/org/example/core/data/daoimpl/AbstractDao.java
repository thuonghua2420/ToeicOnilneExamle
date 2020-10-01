package org.example.core.data.daoimpl;

import org.example.core.common.constant.CoreConstant;
import org.example.core.common.util.HibernateUtil;
import org.example.core.data.dao.GenericDao;
import org.hibernate.*;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public class AbstractDao<ID extends Serializable, T> implements GenericDao<ID, T> {
    private Class<T> persistencceClass;

    public AbstractDao(){
        this.persistencceClass = (Class<T>) ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    }
    public String getPersistenceClassName(){
        return persistencceClass.getSimpleName();
    }
    public List<T> findAll() {
        List<T> list = new ArrayList<T>();
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try{
            transaction = session.beginTransaction();
            //HQL
            StringBuilder sql = new StringBuilder("from ");
            sql.append(this.getPersistenceClassName());
            Query query = session.createQuery(sql.toString());
            list = query.list();
            transaction.commit();

        } catch (HibernateException e){
            transaction.rollback();
            throw e;
        } finally {
            session.close();
        }


        return list;
    }

    public T update(T entity) {
        T result = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try{
            Object object = session.merge(entity);
            result = (T) object;
            transaction.commit();
        } catch (HibernateException e){
            transaction.rollback();
            throw e;
        }finally {
            session.close();
        }
        return null;
    }

    public void save(T entity) {

        T result = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try{
            session.persist(entity);
            transaction.commit();
        } catch (HibernateException e){
            transaction.rollback();
            throw e;
        }finally {
            session.close();
        }
    }

    public T findById(ID id) {
         T result = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try{
               result = (T) session.get(persistencceClass, id);
               if(result == null)
               {
                   throw  new ObjectNotFoundException("NOT NULL" + id, null);
               }
        } catch (HibernateException e){
            transaction.rollback();
            throw e;
        }finally {
            session.close();
        }
        return result;
    }

    public Object[] findByProperty(String property, Object value, String sortExpression, String sortDirection) {
        List<T> list = new ArrayList<T>();
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        Object totalItem =0;
        try{
               StringBuilder sql1 = new StringBuilder("from ");
               sql1.append(getPersistenceClassName());
               if(property != null && value != null){
                   sql1.append(" where ").append(property).append("= :value");
               }
               if(sortDirection != null && sortExpression != null)
               {
                    sql1.append(" order by ").append(sortExpression);
                    sql1.append(" " +(sortDirection.equals(CoreConstant.SORT_ASC)?"asc":"desc"));
               }
               Query query1 = session.createQuery(sql1.toString());
               if(value != null){
                   query1.setParameter("value", value);
               }

               list = query1.list();
               StringBuilder sql2 = new StringBuilder("select count(*) from ");
               sql2.append(getPersistenceClassName());
               if(property != null && value != null){
                   sql2.append("where").append(property).append("= :value");
               }
               Query query2 = session.createQuery(sql2.toString());
               if(value != null){
                   query2.setParameter("value", value);
               }

               totalItem = query2.list().get(0);
               transaction.commit();
        } catch (HibernateException e){
            transaction.rollback();
            throw e;
        }finally {
            session.close();
        }
        return new Object[]{totalItem, list};
    }

    public Integer delete(List<ID> ids) {
        Integer count = 0;
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try{
            for( ID item: ids){
                T t = (T) session.get(persistencceClass, item);
                session.delete(t);
                count++;
            }
            transaction.commit();
        }catch (HibernateException e){
            transaction.rollback();
            throw e;
        }
        return null;
    }

}
