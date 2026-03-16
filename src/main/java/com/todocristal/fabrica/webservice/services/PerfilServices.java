package com.todocristal.fabrica.webservice.services;


import com.todocristal.fabrica.webservice.model.Perfiles;
import java.util.List;

public interface PerfilServices {
	public boolean addPerfil(Perfiles rol) throws Exception;
	public Perfiles getPerfilById(long id) throws Exception;
	public List<Perfiles> getPerfiles() throws Exception;
	
}
