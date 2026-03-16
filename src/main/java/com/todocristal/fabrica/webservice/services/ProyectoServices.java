package com.todocristal.fabrica.webservice.services;


import java.util.List;

import com.todocristal.fabrica.webservice.model.Proyectos;

public interface ProyectoServices {
	public boolean addProyecto(Proyectos proyecto) throws Exception;
	public Proyectos getProyectoById(long id) throws Exception;
        public Proyectos getProyectoByReferencia(String referencia) throws Exception;
        public String getOtrosLacablesProyectoByReferencia(String referencia) throws Exception;
	public List<Proyectos> getProyectos() throws Exception;
	public boolean deleteProyecto(long id) throws Exception;
        public List<Proyectos> getProyectosEnCurso() throws Exception;
        
        public boolean updateProyecto(Proyectos proyecto) throws Exception;
        //public boolean updateProyectoPorReferencia(Proyectos proyecto) throws Exception;
        public Integer getProjectIdCieloByReferencia(Integer referencia) throws Exception;
}
