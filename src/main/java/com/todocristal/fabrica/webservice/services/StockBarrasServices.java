package com.todocristal.fabrica.webservice.services;

/**
 * Copia del objeto Proyectos
 */

import java.util.List;

import com.todocristal.fabrica.webservice.model.StockBarras;

public interface StockBarrasServices {
        //No es necesario versionGestor
	public boolean addBarra(StockBarras barra, boolean versionGestor) throws Exception;
	public StockBarras getBarraById(long id) throws Exception;
        public List<StockBarras> getBarrasByProyecto(Integer referencia) throws Exception;
        /**
         * Obtiene todas las barras compatibles dado el tipo de sistema (CORTINA, CORREDERA...) y color
         * @param color
         * @param tipoSistema
         * @return
         * @throws Exception 
         */
	public List<StockBarras> getStockBarras(String color, String tipoSistema) throws Exception;
        public List<StockBarras> getStockBarras(String color, String tipoSistema, String tipoPerfil) throws Exception;
	public boolean deleteBarra(long id) throws Exception;
        /**
         * Se borra la barra sin conocer el ID a través de los parámetros indicados.
         * @param barra
         * @return 
         * @throws java.lang.Exception 
         */
        public boolean deleteBarra(StockBarras barra) throws Exception;        
        //No se utiliza borrar 29 de Enero 2018
        public List<StockBarras> getBarrasEnCurso() throws Exception;
        
        public boolean updateBarras(List<StockBarras> barras) throws Exception;
        //public boolean updateBarraPorReferencia(Barras proyecto) throws Exception;
        public boolean deleteBarrasByProyecto(Integer proyecto) throws Exception;
        
        /**
         * Agrega un nuevo grupo stock barras completo dado un grupo base desde el cuï¿½l partiremos siempre, normalmente el CRUDO.
         * @param nuevoColor
         * @param listadoGrupoBase
         * @return
         * @throws Exception 
         */
        public boolean agregarNuevoGrupoDadoGrupoBase(String nuevoColor, List<StockBarras> listadoGrupoBase) throws Exception;
        public String cadenaInventarioStockBarras();
        
        /**
         * Devuelve la estructura completa de stockBarras;
         * Se utiliza en gestión de barras de stock android. Evitamos informar los listados manualmente.
         * @return 
         */
        public List<StockBarras> getStockBarras();
}
