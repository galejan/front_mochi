package com.todocristal.fabrica.webservice.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;


import com.todocristal.fabrica.webservice.dao.PerfilDao;
import com.todocristal.fabrica.webservice.model.Perfiles;


public class PerfilServicesImpl implements PerfilServices {

	@Autowired
	PerfilDao PerfilDao;
	
	@Override
	public boolean addPerfil(Perfiles rol) throws Exception {
		return PerfilDao.addPerfil(rol);
	}

	@Override
	public Perfiles getPerfilById(long id) throws Exception {
		return PerfilDao.getPerfilById(id);
	}

	@Override
	public List<Perfiles> getPerfiles() throws Exception {
		return PerfilDao.getPerfiles();
	}

}
