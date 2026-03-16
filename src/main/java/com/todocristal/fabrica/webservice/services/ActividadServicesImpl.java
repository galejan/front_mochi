package com.todocristal.fabrica.webservice.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.todocristal.fabrica.webservice.dao.ActividadDao;
import com.todocristal.fabrica.webservice.model.Actividades;


public class ActividadServicesImpl implements ActividadServices {

	@Autowired
	ActividadDao actividadDao;
	
	@Override
	public boolean addActividad(Actividades actividad) throws Exception {
		return actividadDao.addActividad(actividad);
	}

	@Override
	public Actividades getActividadById(long id) throws Exception {
		return actividadDao.getActividadById(id);
	}

	@Override
	public List<Actividades> getActividades() throws Exception {
		return actividadDao.getActividades();
	}

	@Override
	public boolean deleteActividadByRefyCod(Actividades actividad) throws Exception{
		return actividadDao.deleteActividadByRefyCod(actividad);
	}
        @Override
        public Actividades getActividadByRefyCod(String referencia, Integer codigo) throws Exception{
                return actividadDao.getActividadByRefyCod(referencia, codigo);    
        }
        @Override
        public List<Actividades> getActividadesByProyecto(String referencia) throws Exception{
            return actividadDao.getActividadesByProyecto(referencia);
        }
        @Override
        public boolean updateActividad(Actividades actividad) throws Exception{
            return actividadDao.updateActividad(actividad);
        }
        
        

        
}
