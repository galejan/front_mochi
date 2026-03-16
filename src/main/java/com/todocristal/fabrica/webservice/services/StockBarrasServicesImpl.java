package com.todocristal.fabrica.webservice.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import com.todocristal.fabrica.webservice.dao.StockBarrasDao;


import com.todocristal.fabrica.webservice.model.StockBarras;
import java.math.BigInteger;
import java.util.ArrayList;
import javax.transaction.Transactional;

public class StockBarrasServicesImpl implements StockBarrasServices {

    @Autowired
    StockBarrasDao stockBarrasDao;

    @Override
    public boolean addBarra(StockBarras barra, boolean versionGestor) throws Exception {
            return stockBarrasDao.addBarra(barra, versionGestor);
    }

    @Override
    public StockBarras getBarraById(long id) throws Exception {
            return stockBarrasDao.getBarraById(id);
    }
    @Override
    public List<StockBarras> getBarrasByProyecto(Integer referencia) throws Exception{
        return stockBarrasDao.getBarrasByProyecto(referencia);
    }

    @Override
    public List<StockBarras> getStockBarras(String color, String tipoSistema){
            return stockBarrasDao.getStockBarras(color, tipoSistema);
    }
    @Override
    public List<StockBarras> getStockBarras(String color, String tipoSistema, String tipoPerfil){
            return stockBarrasDao.getStockBarras(color, tipoSistema, tipoPerfil);
    }

    @Override
    public boolean deleteBarra(long id) throws Exception {
            return stockBarrasDao.deleteBarra(id);
    }
    
    @Override
    public boolean deleteBarra(StockBarras barra) throws Exception {
            return stockBarrasDao.deleteBarra(barra);
    }
    
    @Override
    public boolean updateBarras(List<StockBarras> barras) throws Exception{
            return stockBarrasDao.updateBarras(barras);
    }
    /*@Override
    public boolean updateBarraPorReferencia(Barras proyecto) throws Exception{
        return proyectoDao.updateBarraPorReferencia(proyecto);
    }*/


    public List<StockBarras> getBarrasEnCurso() throws  Exception{
        return stockBarrasDao.getBarrasEnCurso();
    }

    @Override
    public boolean deleteBarrasByProyecto(Integer referenciaProyecto) throws Exception {
        stockBarrasDao.deleteBarrasByProyecto(referenciaProyecto);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return true;
    }   
    @Override
    public String cadenaInventarioStockBarras(){
        return cadenaResumenInventarioStockBarras(stockBarrasDao.getStockBarras());        
    }
    
    @Override
    public boolean agregarNuevoGrupoDadoGrupoBase(String nuevoColor, List<StockBarras> listadoGrupoBase) throws Exception{
        return stockBarrasDao.agregarNuevoGrupoDadoGrupoBase(nuevoColor, listadoGrupoBase);
    }
    
    @Override
    public List<StockBarras> getStockBarras(){
        return stockBarrasDao.getStockBarras();
    }
        
    private String cadenaResumenInventarioStockBarras(List<StockBarras> lista){
        Integer auxBarrasCarril = 0;
        Integer auxBarrasCarrilPlus=0;
        Integer auxBarrasCompensador = 0;
        Double auxMetrosBarrasCarril = 0.0;
        Double auxMetrosBarrasCarrilPlus = 0.0;
        Double auxMetrosBarrasCompensador = 0.0;
        Integer totalBarrasCarril =0;
        Integer totalBarrasCarrilPlus = 0;
        Integer totalBarrasCompensador = 0;
        Double totalMetrosCarril = 0.0;
        Double totalMetrosCarrilPlus = 0.0;
        Double totalMetrosCompensador = 0.0;
        List<String> listaColores = new ArrayList<>();
        List<String> listaCadenaResumenColor = new ArrayList<>();
        String auxCadenaCarril = "";
        String auxCadenaCarrilPlus = "";
        String auxCadenaCompensador = "";
        String resultado="";
        Integer longitudBarraCompletaSegunSistema = lista!=null && !lista.isEmpty() && lista.get(0).getTipoSistema().contains("INFINIA") ? 7000 : 6300; 
        
        for(StockBarras sb : lista){
            if(!listaColores.contains(sb.getColor())){
                listaColores.add(sb.getColor());
            }
        }
                
        for(String color : listaColores){
            auxBarrasCarril = 0;
            auxBarrasCarrilPlus = 0;
            auxBarrasCompensador = 0;
            auxMetrosBarrasCarril = 0.0;
            auxMetrosBarrasCarrilPlus = 0.0;
            auxMetrosBarrasCompensador = 0.0;
            
            for(StockBarras sb : lista){
                auxMetrosBarrasCompensador=0.0;
                auxMetrosBarrasCarril = 0.0;
                auxMetrosBarrasCarrilPlus = 0.0;
                auxBarrasCarril = 0;
                auxBarrasCarrilPlus = 0;
                auxBarrasCompensador = 0;
                if(sb.getLongitud() < longitudBarraCompletaSegunSistema && sb.getColor().equals(color)){
                    if(sb.getTipoPerfil().equals("CARRIL") || sb.getTipoPerfil().equals("CARRIL_INV")){
                        auxMetrosBarrasCarril = auxMetrosBarrasCarril + (sb.getLongitud()* sb.getUnidades().doubleValue());
                        totalMetrosCarril += auxMetrosBarrasCarril;
                        auxBarrasCarril = auxBarrasCarril + sb.getUnidades();
                        totalBarrasCarril += auxBarrasCarril;
                        auxCadenaCarril += "Longitud; " + sb.getLongitud() + ";"+ sb.getTipoPerfil() + ";" + sb.getUnidades() +";"+ auxMetrosBarrasCarril.toString()+";<br>";
                    }else if(sb.getTipoPerfil().startsWith("CARRILPLU")){ 
                        auxMetrosBarrasCarrilPlus = auxMetrosBarrasCarrilPlus + (sb.getLongitud()* sb.getUnidades().doubleValue());
                        totalMetrosCarrilPlus += auxMetrosBarrasCarrilPlus;
                        auxBarrasCarrilPlus = auxBarrasCarrilPlus + sb.getUnidades();
                        totalBarrasCarrilPlus += auxBarrasCarrilPlus;
                        auxCadenaCarrilPlus += "Longitud; " + sb.getLongitud() + ";"+ sb.getTipoPerfil() + ";" + sb.getUnidades() +";"+ auxMetrosBarrasCarril.toString()+";<br>";
                    }else if(sb.getTipoPerfil().equals("COMPENSADOR")){
                        auxMetrosBarrasCompensador = auxMetrosBarrasCompensador + (sb.getLongitud()* sb.getUnidades().doubleValue());
                        totalMetrosCompensador += auxMetrosBarrasCompensador;
                        auxBarrasCompensador = auxBarrasCompensador + sb.getUnidades();
                        totalBarrasCompensador += auxBarrasCompensador;                        
                        auxCadenaCompensador += "Longitud; " + sb.getLongitud() + ";"+ sb.getTipoPerfil() + ";" + sb.getUnidades() +"; "+ auxMetrosBarrasCompensador.toString()+";<br>";
                    }else{
                        //Otro tipo de perfil
                    }                    
                }else{
                    // caso de la barra completa
                }
            }
            listaCadenaResumenColor.add(auxCadenaCarril + "<br>" + auxCadenaCarrilPlus + "<br>"+ auxCadenaCompensador + "<br>");            
        }        
        
        totalMetrosCarril = totalMetrosCarril/1000;
        totalMetrosCarrilPlus = totalMetrosCarrilPlus/1000;
        totalMetrosCompensador = totalMetrosCompensador/1000;        
        
        for(int i=0 ; i<listaColores.size();i++){
            resultado += listaColores.get(i) + "<br>" + listaCadenaResumenColor.get(i)+"<br>";
        }
        return  resultado + "<br>Metros totales de stockbarras en inventario CARRIL (" + totalMetrosCarril +"ML) en un número de barras de " +totalBarrasCarril
                + "<br>Metros totales de stockbarras en inventario CARRILPLUS (" + totalMetrosCarrilPlus +"ML) en un número de barras de " +totalBarrasCarrilPlus
                +".<br>  Metros totales de stockbarras en inventario COMPENSADOR (" + totalMetrosCompensador +"ML) en un número de barras de " + totalBarrasCompensador;
        
    }
    
}
