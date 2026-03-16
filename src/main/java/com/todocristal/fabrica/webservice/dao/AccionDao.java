package com.todocristal.fabrica.webservice.dao;

import com.todocristal.fabrica.webservice.model.Acciones;
import java.util.List;

public interface AccionDao {

	public boolean addAccion(Acciones accion) throws Exception;
	public Acciones getAccionById(long id) throws Exception;
	public List<Acciones> getAcciones() throws Exception;
}
