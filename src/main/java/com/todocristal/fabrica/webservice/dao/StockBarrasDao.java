package com.todocristal.fabrica.webservice.dao;


import com.todocristal.fabrica.webservice.model.StockBarras;
import java.util.List;


public interface StockBarrasDao {

	public boolean addBarra(StockBarras barra, boolean versionGestor) throws Exception;
	public StockBarras getBarraById(long id) throws Exception;
	public List<StockBarras> getBarrasByProyecto(Integer referencia) throws Exception;
        public List<StockBarras> getStockBarras(String color, String tipoSistema) ;
        public List<StockBarras> getStockBarras();
        @Deprecated //26/04/2019
	public boolean deleteBarra(long id) throws Exception;
        /**
         * Se unifica la consulta de 
     * @param barra
         * @param barras
         * @return
         * @throws Exception 
         */
        public boolean deleteBarra(StockBarras barra) throws Exception;
        public List<StockBarras> getStockBarras(String color, String tipoSistema, String perfil);        
        public boolean updateBarras(List<StockBarras> barras) throws Exception;
        
        public List<StockBarras> getBarrasEnCurso() throws Exception;
        //public boolean updateBarraPorReferencia(Barras proyecto) throws Exception;
        public boolean deleteBarrasByProyecto(Integer referenciaProyecto);
        public boolean agregarNuevoGrupoDadoGrupoBase(String nuevoColor, List<StockBarras> listadoGrupoBase);
        
}
