package com.todocristal.fabrica.webservice.dao;

import java.util.List;

import com.todocristal.fabrica.webservice.model.Roles;

public interface RolDao {

	public boolean addRol(Roles rol) throws Exception;
	public Roles getRolById(long id) throws Exception;
	public List<Roles> getRoles() throws Exception;
	public boolean deleteRol(long id) throws Exception;
      
        
}
