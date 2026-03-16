package com.todocristal.fabrica.webservice.dao;

import com.todocristal.fabrica.webservice.model.Barras;
import java.util.List;


public interface BarraDao {

	public boolean addBarra(Barras proyecto) throws Exception;
	public Barras getBarraById(long id) throws Exception;
	public List<Barras> getBarrasByProyecto(String referencia) throws Exception;
        public List<Barras> getBarras() throws Exception;
	public boolean deleteBarra(long id) throws Exception;
       
        public boolean updateBarras(List<Barras> barras) throws Exception;
        public boolean borraBarras(List<Barras> barras) throws Exception;
        public List<Barras> getBarrasEnCurso() throws Exception;
        //public boolean updateBarraPorReferencia(Barras proyecto) throws Exception;
        public boolean deleteBarrasByProyecto(String referenciaProyecto);
        public String mochilificarBarrasByProyecto(String referenciaProyecto) throws Exception;
        public String mochilificarBarrasByProyecto(String referenciaProyecto, Integer emisor) throws Exception;
        public String agregaRestosByProyecto (String proyecto, Boolean automantenimiento, Integer codigoActividad) throws Exception;
        public String liberaBarrasByProyecto (String referenciaProyecto) throws Exception;
        public String preparaBarrasByProyecto (String referenciaProyecto) throws Exception;
        //Metodos auxiliares
        public String grosorPerfil(List<Barras> auxBarrasNecesidad) throws Exception;
        public String colorAluminio(List<Barras> auxBarrasNecesidad) throws Exception;
        public Boolean paraLacar (String color, String tipoPerf, String lacadoForzado, List<String> coloresStock) throws Exception;
        public String adaptaColorALacado(String color, String lacadoForzado, Boolean paraLacar) throws Exception;
        public String comprobarSiHayLacadoForzado(List<Barras> auxListaNecesidades) throws Exception;
        public List<String> inicializarColores(String tipoSistema, Boolean status10mm) throws Exception;
        public List<String> inicializarPerfilesMochilificables() throws Exception;
        public List<String> inicializarOtrasBarrasNoMochilificables() throws Exception;
}
