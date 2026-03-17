package com.todocristal.fabrica.webservice.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.todocristal.fabrica.webservice.dao.ProyectoDao;

import com.todocristal.fabrica.webservice.model.Proyectos;
import javax.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ProyectoServicesImpl implements ProyectoServices {

	@Autowired
	ProyectoDao proyectoDao;
	
	@Override
	public boolean addProyecto(Proyectos proyecto) throws Exception {
		return proyectoDao.addProyecto(proyecto);
	}

	@Override
	public Proyectos getProyectoById(long id) throws Exception {
		return proyectoDao.getProyectoById(id);
	}
        @Override
        public Proyectos getProyectoByReferencia(String referencia) throws Exception{
            return proyectoDao.getproyectoByReferencia(referencia);
        }
        @Override
        public String getOtrosLacablesProyectoByReferencia(String referencia) throws Exception{    
            return proyectoDao.getOtrosLacablesProyectoByReferencia(referencia);
        }

	@Override
	public List<Proyectos> getProyectos() throws Exception {
		return proyectoDao.getProyectos();
	}

	@Override
	public boolean deleteProyecto(long id) throws Exception {
		return proyectoDao.deleteProyecto(id);
	}
        @Override
        public boolean updateProyecto(Proyectos proyecto) throws Exception{
                return proyectoDao.updateProyecto(proyecto);
        }
        /*@Override
        public boolean updateProyectoPorReferencia(Proyectos proyecto) throws Exception{
            return proyectoDao.updateProyectoPorReferencia(proyecto);
        }*/
        @Override
        public Integer getProjectIdCieloByReferencia(Integer referencia) throws Exception{
            return proyectoDao.getProjectIdCieloByReferencia(referencia);
        }

    
    public List<Proyectos> getProyectosEnCurso() throws  Exception{
        return proyectoDao.getProyectosEnCurso();
    }
        
       

    

        
       
        
        
}
