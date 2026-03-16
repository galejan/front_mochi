package com.todocristal.fabrica.webservice.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.todocristal.fabrica.webservice.dao.RolDao;
import com.todocristal.fabrica.webservice.model.Roles;


public class RolServicesImpl implements RolServices {

	@Autowired
	RolDao RolDao;
	
	@Override
	public boolean addRol(Roles rol) throws Exception {
		return RolDao.addRol(rol);
	}

	@Override
	public Roles getRolById(long id) throws Exception {
		return RolDao.getRolById(id);
	}

	@Override
	public List<Roles> getRoles() throws Exception {
		return RolDao.getRoles();
	}

	@Override
	public boolean deleteRol(long id) throws Exception {
		return RolDao.deleteRol(id);
	}

        
}
