package com.todocristal.fabrica.webservice.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.todocristal.fabrica.webservice.dao.UsuarioDao;
import com.todocristal.fabrica.webservice.model.Usuarios;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServicesImpl implements UsuarioServices {

	@Autowired
	UsuarioDao usuarioDao;
	
	@Override
	public boolean addUsuario(Usuarios usuario) throws Exception {
		return usuarioDao.addUsuario(usuario);
	}

	@Override
	public Usuarios getUsuarioByNombre(String nombre) throws Exception {
		return usuarioDao.getUsuarioByNombre(nombre);
	}

	@Override
	public List<Usuarios> getUsuarios() throws Exception {
		return usuarioDao.getUsuarios();
	}

	@Override
	public boolean deleteUsuario(long id) throws Exception {
		return usuarioDao.deleteUsuario(id);
	}

        
}
