package com.todocristal.fabrica.webservice.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.todocristal.fabrica.webservice.dao.AccionDao;
import com.todocristal.fabrica.webservice.model.Acciones;


public class AccionServicesImpl implements AccionServices {

	@Autowired
	AccionDao AccionDao;
	
	@Override
	public boolean addAccion(Acciones rol) throws Exception {
		return AccionDao.addAccion(rol);
	}

	@Override
	public Acciones getAccionById(long id) throws Exception {
		return AccionDao.getAccionById(id);
	}

	@Override
	public List<Acciones> getAcciones() throws Exception {
		return AccionDao.getAcciones();
	}

}
