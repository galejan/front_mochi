package com.todocristal.fabrica.webservice.dao;


import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import com.todocristal.fabrica.webservice.model.Acciones;

public class AccionDaoImpl implements AccionDao {

	@Autowired
                
	SessionFactory sessionFactory;

	Session session = null;
	Transaction tx = null;

	@Override
	public boolean addAccion(Acciones accion) throws Exception {

		session = sessionFactory.openSession();
		tx = session.beginTransaction();
		session.save(accion);
		tx.commit();
		session.close();

		return false;
	}
        
      

	@Override
	public Acciones getAccionById(long id) throws Exception {
		session = sessionFactory.openSession();
		Acciones accion = (Acciones) session.load(Acciones.class,
				new Long(id));
		tx = session.getTransaction();
		session.beginTransaction();
		tx.commit();
		return accion;
	}
       

	@SuppressWarnings("unchecked")
	@Override
	public List<Acciones> getAcciones() throws Exception {
		session = sessionFactory.openSession();
		tx = session.beginTransaction();
		List<Acciones> acciones = session.createCriteria(Acciones.class)
				.list();
		tx.commit();
		session.close();
		return acciones;
	}
	

}
