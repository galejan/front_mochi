package com.todocristal.fabrica.webservice.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import com.todocristal.fabrica.webservice.model.Actividades;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

@Repository
public class ActividadDaoImpl implements ActividadDao {

	@Autowired
	SessionFactory sessionFactory;

	Session session = null;
	Transaction tx = null;

	@Override
	public boolean addActividad(Actividades actividad) throws Exception {

		session = sessionFactory.openSession();
		tx = session.beginTransaction();
		session.save(actividad);
		tx.commit();
		session.close();

		return false;
	}
        
	
        @Override
        public Actividades getActividadById(long id) throws Exception{
            session = sessionFactory.openSession();
            Actividades actividad = (Actividades) session.get(Actividades.class, id);
            tx = session.getTransaction();
		session.beginTransaction();
		tx.commit();
            return actividad;
        }

	@SuppressWarnings("unchecked")
	@Override
	public List<Actividades> getActividades() throws Exception {
		session = sessionFactory.openSession();
		tx = session.beginTransaction();
		List<Actividades> actividades = session.createCriteria(Actividades.class)                            
                            .list();
		tx.commit();
		session.close();
		return actividades;
	}
        
        @Override
	public boolean updateActividad(Actividades actividad) throws Exception {

		session = sessionFactory.openSession();
		tx = session.beginTransaction();
		session.update(actividad);
		tx.commit();
		session.close();

		return false;
	}
	
	@Override
	public boolean deleteActividadByRefyCod(Actividades actividad)
			throws Exception {
		session = sessionFactory.openSession();
		//Object o = session.load(Actividades.class, id);
		tx = session.getTransaction();
		session.beginTransaction();
		session.delete(actividad);
		tx.commit();
		return false;
	}
        
        @Override
        public Actividades getActividadByRefyCod(String referencia, Integer codigo) throws Exception{
            /*session = sessionFactory.openSession();*/
            /*Actividades actividad = (Actividades) session.createCriteria(Actividades.class)
                        .add(Restrictions.like("codigo",new String(codigo.toString())))
                        .createCriteria("proyecto")
                            .add(Restrictions.like("referencia",  referencia.toString()))
                        .list();*/
             Actividades actividad = null;
             List<Actividades> actividades = getActividadesByProyecto(referencia);
             for(Actividades a : actividades){
                 if(a.getCodigo().equals(codigo)){
                     actividad = a;
                 }                 
             }
             if(actividad==null){
                 System.out.println("NO SE HA ENCONTRADO LA ACTIVIDAD");
             }
            /*tx = session.getTransaction();
		session.beginTransaction();
		tx.commit();*/
            return actividad;
        }
        
        @Override
        public List<Actividades> getActividadesByProyecto(String referencia) throws Exception{
            session = sessionFactory.openSession();
		tx = session.beginTransaction();
                
                List<Actividades> actividades = (List<Actividades>) session.createCriteria(Actividades.class)
                        .createCriteria("proyecto","p")
                        .add(Restrictions.eq("referencia", referencia)).list();   
                        //.add(Restrictions.("p.referencia", "11111");                
                        
                tx.commit();
		session.close(); 
                return actividades;
                //LA SIGUIENTE PARTE DEL C�DIGO S�LO FUNCIONA CUANDO PODAMOS RECOGER LOS DATOS DE GETACTIVIDADES()
		/*Proyectos proyecto = (Proyectos) session.createCriteria(Proyectos.class)
                            .add(Restrictions.eq("referencia", new String(referencia.toString())))
                            .uniqueResult();                
                Hibernate.initialize(proyecto.getActividades());
		tx.commit();
		session.close();      
                List<Actividades> lista = new ArrayList<Actividades>();
                for(Actividades a : proyecto.getActividades()){
                    lista.add(a);
                }
                */
        }

}
