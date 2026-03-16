package com.todocristal.fabrica.webservice.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import com.todocristal.fabrica.webservice.model.Proyectos;
import java.math.BigInteger;
import java.util.ArrayList;
//import javafx.util.converter.BigIntegerStringConverter;
import javax.transaction.Transactional;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

@Repository
@Transactional  
public class ProyectoDaoImpl implements ProyectoDao {

	@Autowired
	SessionFactory sessionFactory;

	Session session = null;
	Transaction tx = null;

	@Override
	public boolean addProyecto(Proyectos proyecto) throws Exception {

		session = sessionFactory.openSession();
		tx = session.beginTransaction();
		session.save(proyecto);
		tx.commit();
		session.close();

		return false;
	}
        
      	@Override
	public Proyectos getProyectoById(long id) throws Exception {
                session = sessionFactory.openSession();	
                tx = session.beginTransaction();
		Proyectos proyecto = (Proyectos) session.get(Proyectos.class, id);
                tx.commit();
                session.close();
		return proyecto;
	}
        /*
            --------   OBTENEMOS INFORMACIÓN SEGÚN LA REFERENCIA, ASÍ PODEMOS OBTENER EL ID PARA TRATAR EL PROYECTO Y LAS TAREAS.
            EN LA SINCRONIZACIÓN.
        */
        @Override
	public Proyectos getproyectoByReferencia(String referencia) throws Exception{
                session = sessionFactory.openSession();	
                tx = session.beginTransaction();
		
                Proyectos proyecto = (Proyectos) session.createCriteria(Proyectos.class).add(Restrictions.eq("referencia", referencia)).uniqueResult();
                        //.add(Restrictions.eq("referencia", referencia).uniqueResult();
                tx.commit();
                session.close();
		return proyecto;
     
	}
        
        
        
	@SuppressWarnings("unchecked")
	@Override
	public List<Proyectos> getProyectos() throws Exception {
		session = sessionFactory.openSession();
		tx = session.beginTransaction();
		List<Proyectos> proyectos = session.createCriteria(Proyectos.class).list();                
		tx.commit();
		session.close();
                
               // List<Proyectos> proyectos = session.createCriteria(Proyectos.class).list();                
		return proyectos;
	}
	
	@Override
	public boolean deleteProyecto(long id) throws Exception {
		session = sessionFactory.openSession();
		Object o = session.load(Proyectos.class, id);
		tx = session.getTransaction();
		session.beginTransaction();
		session.delete(o);
		tx.commit();
		return false;
	}
            @Override
        public boolean updateProyecto(Proyectos proyecto) throws Exception{
                
                if(proyecto.getId()!=0){                            
                    session = sessionFactory.openSession();
                    tx = session.beginTransaction();
                    session.update(proyecto);
                }else{
                    proyecto.setId(getproyectoByReferencia(proyecto.getReferencia()).getId());        
                    session = sessionFactory.openSession();
                    tx = session.beginTransaction();
                    session.update(proyecto);
                }
		
		tx.commit();
		session.close();

		return false;
        } 
        /*@Override
        public boolean updateProyectoPorReferencia(Proyectos proyecto) throws Exception{            
            session = sessionFactory.openSession();
            tx = session.beginTransaction();            
            session.update(proyecto);
            tx.commit();
            session.close();
            
            return false;
        }*/
        
        // DEVOLVEMOS TODOS LOS PROYECTOS QUE TIENEN ALGUNA ACTIVIDAD QUE NO SE HA TERMINADO(Estado 4), TODO:INEFICIENTE
        @Override
        public List<Proyectos> getProyectosEnCurso() throws Exception{
            List<Proyectos> proyectosEnCurso=null;
            session = sessionFactory.openSession();
            tx = session.beginTransaction();
            
            String strQuery = "SELECT distinct proyectos.id from proyectos "
                    + "left join actividades on actividades.proyecto_id = proyectos.id "
                    + "right join eventos on eventos.actividad_id = actividades.id "
                    + "where eventos.accion_id != 4";
            
            SQLQuery query = session.createSQLQuery(strQuery);
            List<BigInteger> idProyectosEnCurso = (List<BigInteger>)query.list();
            System.out.println(idProyectosEnCurso.toString());
            if(idProyectosEnCurso.size()>0)
                proyectosEnCurso = new ArrayList<Proyectos>();
            for(BigInteger i : idProyectosEnCurso){
                Proyectos p = (Proyectos) session.createCriteria(Proyectos.class)
                        .add(Restrictions.eq("id",  i.longValue())).uniqueResult();
                proyectosEnCurso.add(p);
            }
            
            tx.commit();
            session.close();
           
            return proyectosEnCurso;
        }
        
      
        @Override
        public Integer getProjectIdCieloByReferencia(Integer referencia) throws Exception{
                session = sessionFactory.openSession();	
                tx = session.beginTransaction();
		
                Proyectos proyecto = (Proyectos) session.createCriteria(Proyectos.class).add(Restrictions.eq("referencia", referencia)).uniqueResult();
                        //.add(Restrictions.eq("referencia", referencia).uniqueResult();
                tx.commit();
                session.close();
		return proyecto.getIdCielo();
	}

    @Override
    public String getOtrosLacablesProyectoByReferencia(String referencia) throws Exception {
        session = sessionFactory.openSession();	
                tx = session.beginTransaction();
		
                Proyectos proyecto = (Proyectos) session.createCriteria(Proyectos.class).add(Restrictions.eq("referencia", referencia)).uniqueResult();
                        //.add(Restrictions.eq("referencia", referencia).uniqueResult();
                tx.commit();
                session.close();
                if (proyecto!=null && proyecto.getOtros_lacables()!=null){
                    return proyecto.getOtros_lacables();
                }
		return "";
        
        
    }
}
