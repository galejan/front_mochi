package com.todocristal.fabrica.webservice.dao;

import com.todocristal.fabrica.webservice.model.Perfiles;
import java.util.List;

public interface PerfilDao {

	public boolean addPerfil(Perfiles Perfil) throws Exception;
	public Perfiles getPerfilById(long id) throws Exception;
	public List<Perfiles> getPerfiles() throws Exception;
}
