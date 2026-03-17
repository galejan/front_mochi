package com.todocristal.fabrica.webservice.dao;



import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import com.todocristal.fabrica.webservice.model.LogBarra;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

@Repository
public class LogBarraDaoImpl implements LogBarraDao {

	@Autowired
        SessionFactory sessionFactory;

	Session session = null;
	Transaction tx = null;

	@Override
	public boolean addLogBarra(LogBarra logBarra) throws Exception {

		session = sessionFactory.openSession();
		tx = session.beginTransaction();
		session.save(logBarra);
		tx.commit();
		session.close();

		return false;
	}
        @Override
        public List<LogBarra> getLogBarrasByProyecto(String referencia) throws Exception{
            session = sessionFactory.openSession();	
            tx = session.beginTransaction();

            Criteria crit = session.createCriteria(LogBarra.class)
                   .add(Restrictions.eq("proyecto", referencia.toString()));
            
            List<LogBarra> logBarras = crit.list();

            tx.commit();
            session.close();
            return logBarras;

    }
         
        @Override
        public boolean updateLogBarras(List<LogBarra> logBarras) throws Exception{
            if (logBarras!=null && !logBarras.isEmpty()){
                session = sessionFactory.openSession();
                tx = session.beginTransaction();

                for(LogBarra lb : logBarras){
                    lb.setProcesado(new Timestamp (Calendar.getInstance().getTimeInMillis()));
                    session.update(lb);
                }

                tx.commit();
                session.close();
            }

            return false;
    } 
    
    @Override
    public boolean deleteLogBarrasByProyecto(String referenciaProyecto) {
        
        session = sessionFactory.openSession();
        Criteria crit = session.createCriteria(LogBarra.class)
            .add( Restrictions.eq("proyecto", referenciaProyecto));
            
            List<LogBarra> logBarras = crit.list();
            for(LogBarra lb : logBarras){
                session.delete(lb);
            }
            tx = session.getTransaction();
            session.beginTransaction();
            
            tx.commit();
            return false;
    }
}
