package com.todocristal.fabrica.webservice.services;

import com.todocristal.fabrica.webservice.model.Actividades;
import java.util.List;

public interface ActividadServices {
	public boolean addActividad(Actividades actividad) throws Exception;
	public Actividades getActividadById(long id) throws Exception;
	public List<Actividades> getActividades() throws Exception;
	public boolean deleteActividadByRefyCod(Actividades actividad) throws Exception;
        
        public Actividades getActividadByRefyCod(String referencia, Integer codigo) throws Exception;
        public List<Actividades> getActividadesByProyecto(String referencia) throws Exception;
        public boolean updateActividad(Actividades actividad) throws Exception;
        
        
}
