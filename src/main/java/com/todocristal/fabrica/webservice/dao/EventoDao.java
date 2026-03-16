package com.todocristal.fabrica.webservice.dao;

import com.todocristal.fabrica.webservice.model.Eventos;
import java.util.List;

public interface EventoDao {
	public boolean addEvento(Eventos tiempo) throws Exception;
	public Eventos getEventoById(long id) throws Exception;
	public List<Eventos> getEventos() throws Exception;
	public boolean deleteEvento(long id) throws Exception;
        
        public List<Eventos> getEventoByActividad(long id) throws Exception;
        public List<Eventos> getEventoByActividadUsuario(long id, String usuario) throws Exception; 
        public List<Eventos> getEventosByUsuarioId (long idUsuario) throws Exception;
        //public Boolean getAuxEventoActividadExiste(long actividad_id) throws Exception;
        public boolean updateEvento(Eventos evento) throws Exception;
}
