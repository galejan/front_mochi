package com.todocristal.fabrica.webservice.dao;

import java.util.List;

import com.todocristal.fabrica.webservice.model.Usuarios;

public interface UsuarioDao {

	public boolean addUsuario(Usuarios usuario) throws Exception;
	public Usuarios getUsuarioByNombre(String nombre) throws Exception;
	public List<Usuarios> getUsuarios() throws Exception;
	public boolean deleteUsuario(long id) throws Exception;
       
        
}
