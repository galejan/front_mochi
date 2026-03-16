package com.todocristal.fabrica.webservice.services;

import com.todocristal.fabrica.webservice.model.Acciones;
import java.util.List;

public interface AccionServices {
	public boolean addAccion(Acciones rol) throws Exception;
	public Acciones getAccionById(long id) throws Exception;
	public List<Acciones> getAcciones() throws Exception;
	//public boolean deleteAccion(long id) throws Exception;
}
