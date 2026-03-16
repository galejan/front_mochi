package com.todocristal.fabrica.webservice.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.todocristal.fabrica.webservice.dao.BarraDao;

import com.todocristal.fabrica.webservice.model.Barras;

public class BarraServicesImpl implements BarraServices {

    @Autowired
    BarraDao barraDao;

    @Override
    public boolean addBarra(Barras barra) throws Exception {
            return barraDao.addBarra(barra);
    }

    @Override
    public Barras getBarraById(long id) throws Exception {
            return barraDao.getBarraById(id);
    }
    @Override
    public List<Barras> getBarrasByProyecto(String referencia) throws Exception{
        return barraDao.getBarrasByProyecto(referencia);
    }

    @Override
    public List<Barras> getBarras() throws Exception {
            return barraDao.getBarras();
    }

    @Override
    public boolean deleteBarra(long id) throws Exception {
            return barraDao.deleteBarra(id);
    }
    @Override
    public boolean updateBarras(List<Barras> barras) throws Exception{
            return barraDao.updateBarras(barras);
    }
    @Override
    public boolean borraBarras(List<Barras> barras) throws Exception{
            return barraDao.borraBarras(barras);
    }
    
    
    /*@Override
    public boolean updateBarraPorReferencia(Barras proyecto) throws Exception{
        return proyectoDao.updateBarraPorReferencia(proyecto);
    }*/


    public List<Barras> getBarrasEnCurso() throws  Exception{
        return barraDao.getBarrasEnCurso();
    }

    @Override
    public boolean deleteBarrasByProyecto(String referenciaProyecto) throws Exception {
        barraDao.deleteBarrasByProyecto(referenciaProyecto);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return true;
    }
    
    @Override
    public String mochilificarBarrasByProyecto(String referenciaProyecto) throws Exception {
        return barraDao.mochilificarBarrasByProyecto(referenciaProyecto);        
         
    }
    
    @Override
    public String mochilificarBarrasByProyecto(String referenciaProyecto, Integer emisor) throws Exception {
        return barraDao.mochilificarBarrasByProyecto(referenciaProyecto, emisor);        
         
    }
    
    @Override
    public String agregaRestosByProyecto (String referenciaProyecto, Boolean automantenimiento, Integer codigoActividad) throws Exception {
        
        return barraDao.agregaRestosByProyecto(referenciaProyecto, automantenimiento,codigoActividad);
    }
    
    @Override
    public boolean liberaBarrasByProyecto (String referenciaProyecto) throws Exception {
        barraDao.liberaBarrasByProyecto(referenciaProyecto);
        return true;
    }
    
    @Override
    public boolean preparaBarrasByProyecto (String referenciaProyecto) throws Exception {
        barraDao.preparaBarrasByProyecto(referenciaProyecto);
        return true;
    }
    
    // Metodos auxiliares
    
    @Override
    public String grosorPerfil(List<Barras> auxBarrasNecesidad) throws Exception {
        return barraDao.grosorPerfil(auxBarrasNecesidad);
        
    }
    
    @Override
    public String colorAluminio(List<Barras> auxBarraaNecesidad)throws Exception {
        return barraDao.colorAluminio(auxBarraaNecesidad);
    }
    
    @Override
    public Boolean paraLacar (String color, String tipoPerf, String lacadoForzado, List<String> coloresStock) throws Exception {
        return barraDao.paraLacar(color, tipoPerf, lacadoForzado, coloresStock);
        
    }
    
    @Override
    public String adaptaColorALacado(String color, String lacadoForzado, Boolean paraLacar) throws Exception {
        return barraDao.adaptaColorALacado(color, lacadoForzado, paraLacar);
        
    }

    @Override
    public String comprobarSiHayLacadoForzado(List<Barras> auxListaNecesidades) throws Exception {
        return barraDao.comprobarSiHayLacadoForzado(auxListaNecesidades);
        
    }
    
    @Override
    public List<String> inicializarColores(String tipoSistema, Boolean status10mm) throws Exception{
        return barraDao.inicializarColores(tipoSistema, status10mm);
    }
    
    @Override
    public List<String> inicializarPerfilesMochilificables() throws Exception{
        return barraDao.inicializarPerfilesMochilificables();
    }

    @Override
    public List<String> inicializarOtrasBarrasNoMochilificables() throws Exception{
        return barraDao.inicializarOtrasBarrasNoMochilificables();
    }    
       
        
        
}
