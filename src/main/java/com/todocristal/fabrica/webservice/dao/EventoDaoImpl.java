package com.todocristal.fabrica.webservice.dao;


import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import com.todocristal.fabrica.webservice.model.Eventos;
import org.hibernate.criterion.Restrictions;

public class EventoDaoImpl implements EventoDao {

	@Autowired
                
	SessionFactory sessionFactory;

	Session session = null;
	Transaction tx = null;

	@Override
	public boolean addEvento(Eventos evento) throws Exception {

		session = sessionFactory.openSession();
		tx = session.beginTransaction();
		session.save(evento);
		tx.commit();
		session.close();

		return false;
	}

	@Override
	public Eventos getEventoById(long id) throws Exception {
		session = sessionFactory.openSession();
                 Eventos evento = (Eventos) session.createCriteria(Eventos.class)
                        .add(Restrictions.eq("id", id)).uniqueResult();		
		tx = session.getTransaction();
		session.beginTransaction();
		tx.commit();
		return evento;
	}
        

	@SuppressWarnings("unchecked")
	@Override
	public List<Eventos> getEventos() throws Exception {
		session = sessionFactory.openSession();
		tx = session.beginTransaction();
		List<Eventos> eventos = session.createCriteria(Eventos.class)
				.list();
		tx.commit();
		session.close();
		return eventos;
	}
	
	@Override
	public boolean deleteEvento(long id)
			throws Exception {
		session = sessionFactory.openSession();
		Object o = session.load(Eventos.class, id);
		tx = session.getTransaction();
		session.beginTransaction();
		session.delete(o);
		tx.commit();
		return false;
	}

        @Override
        public List<Eventos> getEventoByActividad(long id) throws Exception {
            session = sessionFactory.openSession();
		tx = session.beginTransaction();
		 
                /*Actividades actividad = (Actividades) session.createCriteria(Actividades.class)                            
                                .add(Restrictions.eq("id", id))
				.uniqueResult();                
                
               Hibernate.initialize(actividad.getListaEventos());*/
                List<Eventos> eventos = (List<Eventos>) session.createCriteria(Eventos.class)
                            .add(Restrictions.eq("actividad.id", id))
                            .list();
                
		tx.commit();
		session.close();
		return eventos; //actividad.getListaEventos();
        }

        /*
            Listado de eventos realizados por un usuario determinado en la actividad, OK
        Nota:  no están prefiltradas por los criterios createAlias.
        */
        @Override
        public List<Eventos> getEventoByActividadUsuario(long id, String usuario) throws Exception {
            session = sessionFactory.openSession();
		tx = session.beginTransaction();
		
                        List<Eventos> eventos = (List<Eventos>) session.createCriteria(Eventos.class)
                        .createAlias("usuario", "us")
                        .createAlias("actividad", "ac")
                        .add(Restrictions.eq("us.usuario", usuario))                                
                        .add(Restrictions.eq("ac.id", id)).list();
		tx.commit();
		session.close();
		return eventos;
        }

    @Override
    public List<Eventos> getEventosByUsuarioId(long idUsuario) throws Exception {
        session = sessionFactory.openSession();
		tx = session.beginTransaction();
		List<Eventos> eventos = session.createCriteria(Eventos.class).createCriteria("usuario").add(Restrictions.eq("id", idUsuario)).list();
		tx.commit();
		session.close();
		return eventos;
    }
    
    @Override
    public boolean updateEvento(Eventos evento) throws Exception {
        session = sessionFactory.openSession();
        tx = session.beginTransaction();
        session.update(evento);
        tx.commit();
        session.close();

        return false;
    }
    
    /*
        AUXILIAR PARA COMPROBAR SI EXISTE AL MENOS UN EVENTO DE UNA ACTIVIDAD.
   
    public Boolean getAuxEventoActividadExiste(long actividad_id){
        Eventos e = (Eventos) sessionFactory.getCurrentSession().createCriteria(Eventos.class)
                .add(Restrictions.eq("actividad_id", actividad_id)).uniqueResult();
        return (e!=null && e.getActividad().getId()==actividad_id);
    } */

}
