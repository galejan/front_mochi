package com.todocristal.fabrica.webservice.services;

import java.util.List;

import com.todocristal.fabrica.webservice.model.Usuarios;

public interface UsuarioServices {
	public boolean addUsuario(Usuarios usuario) throws Exception;
	public Usuarios getUsuarioByNombre(String nombre) throws Exception;
	public List<Usuarios> getUsuarios() throws Exception;
	public boolean deleteUsuario(long id) throws Exception;
}
