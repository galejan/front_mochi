package com.todocristal.fabrica.webservice.services;


import org.springframework.beans.factory.annotation.Autowired;

import com.todocristal.fabrica.webservice.dao.LogBarraDao;
import com.todocristal.fabrica.webservice.model.LogBarra;
import java.util.List;


public class LogBarraServicesImpl implements LogBarraServices {

    @Autowired
    LogBarraDao logBarraDao;

    @Override
    public boolean addLogBarra(LogBarra logBarra) throws Exception {
        return logBarraDao.addLogBarra(logBarra);
    }

    @Override
    public List<LogBarra> getLogBarrasByProyecto(String referencia) throws Exception {
        return logBarraDao.getLogBarrasByProyecto(referencia);
    }

    @Override
    public boolean updateLogBarras(List<LogBarra> logBarras) throws Exception {
        return logBarraDao.updateLogBarras(logBarras);
    }

    @Override
    public boolean deleteLogBarrasByProyecto(String referenciaProyecto) throws Exception {
        logBarraDao.deleteLogBarrasByProyecto(referenciaProyecto);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return true;
    }
}
