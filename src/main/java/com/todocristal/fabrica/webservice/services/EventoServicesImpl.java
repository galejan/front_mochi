package com.todocristal.fabrica.webservice.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.todocristal.fabrica.webservice.dao.EventoDao;
import com.todocristal.fabrica.webservice.model.Eventos;


public class EventoServicesImpl implements EventoServices {

	@Autowired
	EventoDao eventoDao;
	
	@Override
	public boolean addEvento(Eventos tiempo) throws Exception {
		return eventoDao.addEvento(tiempo);
	}

	@Override
	public Eventos getEventoById(long id) throws Exception {
		return eventoDao.getEventoById(id);
	}

	@Override
	public List<Eventos> getEventos() throws Exception {
		return eventoDao.getEventos();
	}

	@Override
	public boolean deleteEvento(long id) throws Exception {
		//return eventoDao.deleteEvento(id);
            throw new UnsupportedOperationException("Not supported yet."); 
	}

        @Override
        public List<Eventos> getEventosByActividad(long id) throws Exception {
            return eventoDao.getEventoByActividad(id);
        }

        @Override
        public List<Eventos> getEventosByActividadUsuario(long id, String proyecto) throws Exception {
            return eventoDao.getEventoByActividadUsuario(id, proyecto);
        }
        
        @Override
        public List<Eventos> getEventosByUsuarioId(long idUsuario) throws Exception{
            return eventoDao.getEventosByUsuarioId(idUsuario);
        }
        /*@Override
        public Boolean getAuxEventoActividadExiste(long actividad_id) throws Exception{
            return eventoDao.getAuxEventoActividadExiste(actividad_id);
        }*/
        @Override
        public boolean updateEventos(Eventos evento) throws Exception{
            return eventoDao.updateEvento(evento);
        }

    
       
}
