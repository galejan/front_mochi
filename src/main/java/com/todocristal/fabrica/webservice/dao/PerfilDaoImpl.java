package com.todocristal.fabrica.webservice.dao;


import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import com.todocristal.fabrica.webservice.model.Acciones;
import com.todocristal.fabrica.webservice.model.Perfiles;
import org.springframework.stereotype.Repository;

@Repository
public class PerfilDaoImpl implements PerfilDao {

	@Autowired
                
	SessionFactory sessionFactory;

	Session session = null;
	Transaction tx = null;

	@Override
	public boolean addPerfil(Perfiles perfil) throws Exception {

		session = sessionFactory.openSession();
		tx = session.beginTransaction();
		session.save(perfil);
		tx.commit();
		session.close();

		return false;
	}
        
      

	@Override
	public Perfiles getPerfilById(long id) throws Exception {
		session = sessionFactory.openSession();
		Perfiles perfil = (Perfiles) session.load(Perfiles.class,
				new Long(id));
		tx = session.getTransaction();
		session.beginTransaction();
		tx.commit();
		return perfil;
	}
       

	@SuppressWarnings("unchecked")
	@Override
	public List<Perfiles> getPerfiles() throws Exception {
		session = sessionFactory.openSession();
		tx = session.beginTransaction();
		List<Perfiles> perfiles = session.createCriteria(Acciones.class).list();
		tx.commit();
		session.close();
		return perfiles;
	}
	

}
