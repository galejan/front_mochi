package com.todocristal.fabrica.webservice.dao;


import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import com.todocristal.fabrica.webservice.model.Roles;
import org.springframework.stereotype.Repository;

@Repository
public class RolDaoImpl implements RolDao {

	@Autowired
                
	SessionFactory sessionFactory;

	Session session = null;
	Transaction tx = null;

	@Override
	public boolean addRol(Roles rol) throws Exception {

		session = sessionFactory.openSession();
		tx = session.beginTransaction();
		session.save(rol);
		tx.commit();
		session.close();

		return false;
	}
        
      

	@Override
	public Roles getRolById(long id) throws Exception {
		session = sessionFactory.openSession();
		Roles rol = (Roles) session.load(Roles.class,
				new Long(id));
		tx = session.getTransaction();
		session.beginTransaction();
		tx.commit();
		return rol;
	}
       

	@SuppressWarnings("unchecked")
	@Override
	public List<Roles> getRoles() throws Exception {
		session = sessionFactory.openSession();
		tx = session.beginTransaction();
		List<Roles> roles = session.createCriteria(Roles.class)
				.list();
		tx.commit();
		session.close();
		return roles;
	}
	
	@Override
	public boolean deleteRol(long id)
			throws Exception {
		session = sessionFactory.openSession();
		Object o = session.load(Roles.class, id);
		tx = session.getTransaction();
		session.beginTransaction();
		session.delete(o);
		tx.commit();
		return false;
	}

}
