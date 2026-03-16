package com.todocristal.fabrica.webservice.dao;


import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import com.todocristal.fabrica.webservice.model.Usuarios;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

public class UsuarioDaoImpl implements UsuarioDao {

	@Autowired                
	SessionFactory sessionFactory;

	Session session = null;
	Transaction tx = null;

	@Override
	public boolean addUsuario(Usuarios usuario) throws Exception {

		session = sessionFactory.openSession();
		tx = session.beginTransaction();
		session.save(usuario);
		tx.commit();
		session.close();

		return false;
	}

	@Override
	public Usuarios getUsuarioByNombre(String nombre) throws Exception {
		session = sessionFactory.openSession();
		List<Usuarios> usuarios = (List<Usuarios>)  session.createCriteria(Usuarios.class)
                        .add(Restrictions.like("usuario", nombre)).list();
		tx = session.getTransaction();
		session.beginTransaction();
		tx.commit();                
                
		return usuarios.get(0);
	}
       

	@SuppressWarnings("unchecked")        
	@Override
	public List<Usuarios> getUsuarios() throws Exception {
		session = sessionFactory.openSession();
		tx = session.beginTransaction();
		List<Usuarios> usuarios = session.createCriteria(Usuarios.class)
				.list();
		tx.commit();
		session.close();
		return usuarios;
	}
	
	@Override
	public boolean deleteUsuario(long id)
			throws Exception {
		session = sessionFactory.openSession();
		Object o = session.load(Usuarios.class, id);
		tx = session.getTransaction();
		session.beginTransaction();
		session.delete(o);
		tx.commit();
		return false;
	}

}
