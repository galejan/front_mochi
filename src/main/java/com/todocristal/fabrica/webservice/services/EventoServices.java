package com.todocristal.fabrica.webservice.services;

import java.util.List;
import com.todocristal.fabrica.webservice.model.Eventos;

public interface EventoServices {
	public boolean addEvento(Eventos evento) throws Exception;
	public Eventos getEventoById(long id) throws Exception;
	public List<Eventos> getEventos() throws Exception;
	public boolean deleteEvento(long id) throws Exception;
        
        public List<Eventos> getEventosByActividad(long id_actividad) throws Exception;
        public List<Eventos> getEventosByActividadUsuario(long id_actividad, String proyecto) throws Exception;
        public List<Eventos> getEventosByUsuarioId(long idUsuario) throws Exception;
        //public Boolean getAuxEventoActividadExiste(long actividad_id) throws Exception;
        public boolean updateEventos(Eventos evento) throws Exception;
}
