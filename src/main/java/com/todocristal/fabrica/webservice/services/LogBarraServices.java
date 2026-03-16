package com.todocristal.fabrica.webservice.services;

import com.todocristal.fabrica.webservice.model.LogBarra;
import java.util.List;

public interface LogBarraServices {
	public boolean addLogBarra(LogBarra logBarra) throws Exception;
        public List<LogBarra> getLogBarrasByProyecto(String referencia) throws Exception;
        public boolean updateLogBarras(List<LogBarra> logBarras) throws Exception;
        public boolean deleteLogBarrasByProyecto(String referenciaProyecto) throws Exception; 
}
