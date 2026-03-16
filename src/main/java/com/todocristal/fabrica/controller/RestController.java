package com.todocristal.fabrica.controller;


import com.todocristal.fabrica.webservice.auxiliar.StockBarrasAndroid;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.todocristal.fabrica.webservice.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.todocristal.fabrica.webservice.model.Actividades;
import com.todocristal.fabrica.webservice.model.Barras;
import com.todocristal.fabrica.webservice.model.StockBarras;
import com.todocristal.fabrica.webservice.model.Proyectos;
import com.todocristal.fabrica.webservice.model.Roles;
import com.todocristal.fabrica.webservice.model.Eventos;
import com.todocristal.fabrica.webservice.model.LogBarra;
import com.todocristal.fabrica.webservice.model.Usuarios;
import com.todocristal.fabrica.webservice.services.ActividadServices;
import com.todocristal.fabrica.webservice.services.BarraServices;

import com.todocristal.fabrica.webservice.services.ProyectoServices;
import com.todocristal.fabrica.webservice.services.RolServices;
import com.todocristal.fabrica.webservice.services.EventoServices;
import com.todocristal.fabrica.webservice.services.LogBarraServices;
import com.todocristal.fabrica.webservice.services.StockBarrasServices;
import com.todocristal.fabrica.webservice.services.UsuarioServices;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import utilidades.FileUtil;
import utilidades.GeneradorXML;




@Controller
@RequestMapping("/")
public class RestController {

	@Autowired
	ProyectoServices proyectoServices;        
        @Autowired
	EventoServices eventoServices;
        @Autowired
	RolServices rolServices;
        @Autowired
	UsuarioServices usuarioServices;        
        @Autowired
	ActividadServices actividadServices;
        @Autowired
	BarraServices barraServices;
        @Autowired
        StockBarrasServices stockBarraServices;
        @Autowired
        LogBarraServices logBarraServices;
        
	static final Logger logger = LoggerFactory.getLogger(RestController.class);
        Boolean automantenimiento = Boolean.FALSE;  // lo usaremos para poder desactivar el borrado automatico. 
        
        /**
         * URI REST PROYECTOS
     * @param proyecto
     * @return 
         */
	@RequestMapping(value = "proyectos", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Status addProyecto(@RequestBody Proyectos proyecto) {
		try {                    
                    if(proyectoServices.getProyectoByReferencia(proyecto.getReferencia())!=null){
                        if(!bloqueoProyecto(proyectoServices.getProyectoByReferencia(proyecto.getReferencia())) ){
                            proyectoServices.updateProyecto(proyecto);
                            barraServices.liberaBarrasByProyecto(proyecto.getReferencia());
                            barraServices.deleteBarrasByProyecto(proyecto.getReferencia());
                        }else{
                            //No hacer nada.
                            return new Status(1, "No se agrega el proyecto por bloqueo por eventos de cortes en marcha.!");
                        }
                        
                    }else{                        
                        proyectoServices.addProyecto(proyecto);
                    }
			
                    return new Status(1, "Proyecto agregado correctamente!");
		} catch (Exception e) {
			// e.printStackTrace();
			return new Status(0, e.toString());
		}

	}
        /**
         * Actualizar proyectos ajustado para recibir proyectos son ID y sin ID (re-sincronizados)
         * @param proyecto
         * @return 
         */
        @RequestMapping(value = "proyectos", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Status updateProyecto(@RequestBody Proyectos proyecto) {
		try {
                    if(proyecto.getId()==0){
                        proyecto.setId(proyectoServices.getProyectoByReferencia(proyecto.getReferencia()).getId());			
                    }
                    proyectoServices.updateProyecto(proyecto);
                    return new Status(1, "Proyecto actualizado correctamente!");
		} catch (Exception e) {
			// e.printStackTrace();
			return new Status(0, e.toString());
		}

	}
        /**
         * Este metodo permite actualizar un proyecto que no contiene id para identificar el registro.
         * @param proyecto
         * @return 
         
        @RequestMapping(value = "proyectos/navision", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Status updateProyectoPorReferencia(@RequestBody Proyectos proyecto) {
		try {
                        proyecto.setId(proyectoServices.getProyectoByReferencia(proyecto.getReferencia()).getId());
			proyectoServices.updateProyecto(proyecto);
			return new Status(1, "Proyecto actualizado correctamente!");
		} catch (Exception e) {
			// e.printStackTrace();
			return new Status(0, e.toString());
		}

	}*/
        
        @RequestMapping(value = "proyectos/{id}", method = RequestMethod.GET )
	public @ResponseBody
	Proyectos getProyectoById(@PathVariable("id") long id) {
		Proyectos proyecto = null;
		try {
			proyecto = proyectoServices.getProyectoById(id);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return proyecto;
	}
        
        @RequestMapping(value = "proyectos/navision/{ref}", method = RequestMethod.GET )
	public @ResponseBody
	Proyectos getProyectoByRef(@PathVariable("ref") String referencia) {
		Proyectos proyecto = null;
		try {
			proyecto = proyectoServices.getProyectoByReferencia(referencia);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return proyecto;
	}
        

	@RequestMapping(value = "proyectos", method = RequestMethod.GET)
	public @ResponseBody
	List<Proyectos> getProyectos() {

		List<Proyectos> proyectos = null ;
		try {
			proyectos = proyectoServices.getProyectos() ;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return proyectos;
	}
        /*
            LOS PROYECTOS EN CURSO SON LOS QUE A�N LE QUEDAN ACTIVIDADES POR FINALIZAR 
        */
        @RequestMapping(value = "proyectos/encurso", method = RequestMethod.GET)
        public @ResponseBody
        List<Proyectos> getProyectosEnCurso(){
            List<Proyectos> proyectosEnCurso = null;
            try{
                proyectosEnCurso = proyectoServices.getProyectosEnCurso();
            } catch (Exception e){
                e.printStackTrace();
            }
            return proyectosEnCurso;
        }
        
        @RequestMapping(value = "proyectos/idCielo/{ref}", method = RequestMethod.GET )
	public @ResponseBody
	Integer getProjectIdCieloByReferencia(@PathVariable("ref") Integer referencia) {
		Integer idCielo = null;
		try {
			idCielo = proyectoServices.getProjectIdCieloByReferencia(referencia);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return idCielo;
	}
        
        /**
         * URI REST ACTIVIDADES
         */
        //AGREGA UNA ACTIVIDAD SI Y SOLO SI EXISTE EL OBJETO PROYECTO AL QUE DEBE HACER REFERENCIA
        //POR LA DEFINICI�N DE RESTRICCIONES DE LA BASE DE DATOS NO HABR� ACTIVIDADES DUPLICADAS (REF PROYECTO y CODIGO ACTIVIDAD)
        //CREAMOS UN EVENTO POR CADA ACTIVIDAD QUE NO TENGA EVENTO, TODO: OPTIMIZAR CON UNA CONSULTA CRITERIA o CON CONSULTA QUE COMPRUEBA SI EXISTE ALG�N EVENTO DEL PROYECTO
        @RequestMapping(value = "proyectos/actividades/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Status addActividad(@RequestBody Actividades actividad) {
		try { 
                        if(getActividadByRefyCod(actividad.getProyecto().getReferencia(), actividad.getCodigo())!=null){
                            return new Status(0, "Actividad en la BDD, podria ser duplicada, no ha sido posible agregar.");
                        }else{ // NO ESTA DUPLICADA                            
                        
                            actividadServices.addActividad(actividad);   
                            Actividades ac = actividadServices.getActividadByRefyCod( actividad.getProyecto().getReferencia(), actividad.getCodigo());
                            if(ac==null ){  //AGREGAMOS EVENTO POR DEFECTO
                                Eventos e = new Eventos();
                                e.setAccion(null);
                                e.setActividad(ac);
                                e.setRegistro(new Timestamp (Calendar.getInstance().getTimeInMillis()));                            
                                e.setUsuario(null);

                                eventoServices.addEvento(e);
                            }
                            
                            return new Status(1, "Actividad agregada correctamente!");
                        }
                        
			
		} catch (Exception e) {
			// e.printStackTrace();
			return new Status(0, e.toString());
		}

	}
        @RequestMapping(value = "proyectos/actividades/", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Status updateActividad(@RequestBody Actividades actividad) {
		try {
			actividadServices.updateActividad(actividad);
			return new Status(1, "Actividad actualizada correctamente!");
		} catch (Exception e) {
			// e.printStackTrace();
			return new Status(0, e.toString());
		}
	}
        
        @RequestMapping(value = "proyectos/{ref}/actividades/{cod}", method = RequestMethod.GET)
	public @ResponseBody
	Actividades getActividadByRefyCod( @PathVariable("ref") String referencia, @PathVariable("cod") Integer codigo) {
		Actividades actividad = null;
		try {
			actividad = actividadServices.getActividadByRefyCod(referencia, codigo);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return actividad;
	}
        

	@RequestMapping(value = "proyectos/actividades", method = RequestMethod.GET)
	public @ResponseBody
	List<Actividades> getActividades() {

		List<Actividades> actividades = null;
		try {
			actividades = actividadServices.getActividades();
                        
		} catch (Exception e) {
			e.printStackTrace();
		}

		return actividades;
	}
        
        @RequestMapping(value = "proyectos/{ref}/actividades", method = RequestMethod.GET)
	public @ResponseBody
	List<Actividades> getActividadesByProyecto(@PathVariable("ref") String referencia) {

		List<Actividades> actividades = null;
		try {
			actividades = actividadServices.getActividadesByProyecto(referencia);
                        
		} catch (Exception e) {
			e.printStackTrace();
		}

		return actividades;
	}
        
        @RequestMapping(value = "proyectos/{ref}/actividades/{cod}", method = RequestMethod.DELETE)
	public @ResponseBody
	Status deleteActividades( @PathVariable("referencia") String referencia, @PathVariable("cod") Integer codigo) {

		try {
			actividadServices.deleteActividadByRefyCod(getActividadByRefyCod(referencia, codigo));
			return new Status(1, "Actividad eliminada correctamente!");
		} catch (Exception e) {
			return new Status(0, e.toString());
		}

	}
        @RequestMapping(value = "proyectos/actividades/{id}", method = RequestMethod.GET)
	public @ResponseBody
	Actividades getActividadById(@PathVariable("id") long id) {
		Actividades actividad = null;
		try {
			actividad = actividadServices.getActividadById(id);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return actividad;
	}
        
        /* TODO: Obtener todas las barras de la BDD*/
        
        @RequestMapping(value = "proyectos/barras/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Status addBarras(@RequestBody Barras barra) {
		try { 

                    barraServices.addBarra(barra);                    
                    return new Status(1, "Barra agregada correctamente!");
			
		} catch (Exception e) {
			// e.printStackTrace();
                    return new Status(0, e.toString());
		}
	}
        
        @RequestMapping(value = "proyectos/{ref}/barras", method = RequestMethod.GET)
	public @ResponseBody
	List<Barras> getBarrasByProyecto(@PathVariable("ref") String referencia) {

		List<Barras> barras = null;
		try {
                    barras = barraServices.getBarrasByProyecto(referencia);
                        
		} catch (Exception e) {
			e.printStackTrace();
		}
		return barras;
	}
        
        @RequestMapping(value = "proyectos/{ref}/barras/delete/", method = RequestMethod.POST) //TODO: investsigar para utilizar DELETE
	public @ResponseBody
	Status deleteBarrasByProyecto(@PathVariable("ref") String referencia) {

		try {
                    barraServices.deleteBarrasByProyecto(referencia);
                    return new Status(1, "barras eliminadas correctamente del proyecto:" + referencia +"!");
		} catch (Exception e) {
                    return new Status(0, e.toString());
		}

	}
        
        @RequestMapping(value = "proyectos/{ref}/barras/autodelete/", method = RequestMethod.POST) //TODO: investsigar para utilizar DELETE
	public @ResponseBody
	Status autoDeleteBarrasByProyecto(@PathVariable("ref") String referencia) {
		try {
                    if (automantenimiento){
                        barraServices.deleteBarrasByProyecto(referencia);
                        return new Status(1, "barras eliminadas correctamente del proyecto:" + referencia +"!");
                    } else {
                        return new Status(1, "Barras no eliminadas por bloqueo de automantenimiento");
                    }
		} catch (Exception e) {
                    return new Status(0, e.toString());
		}
                
	}
        
        @RequestMapping(value = "proyectos/{ref}/mochilificar/", method = RequestMethod.GET) //TODO: investsigar para utilizar DELETE
	public @ResponseBody
	Status mochilificarBarrasByProyecto(@PathVariable("ref") String referencia) {
                String cadenaConsumo = "";
                List<Barras> auxBarras = new ArrayList<>();
		try {
                    cadenaConsumo = barraServices.mochilificarBarrasByProyecto(referencia);
                    auxBarras.addAll(barraServices.getBarrasByProyecto(referencia));
                    GeneradorXML generador = new GeneradorXML(barraServices,stockBarraServices);
                    generador.guardarArchivo(referencia, Integer.valueOf(auxBarras.get(0).getIdentificadorUnicoBarra().substring(6,7)), false);
                    return new Status(1, "barras del proyecto " + referencia +", mochilificadas correctamente! . Consumo: "+cadenaConsumo);
		} catch (Exception e) {
                    return new Status(0, e.toString());
		}

	}
        
        @RequestMapping(value = "proyectos/{ref}/mochilificar/{emi}", method = RequestMethod.GET) //TODO: investsigar para utilizar DELETE
	public @ResponseBody
	Status mochilificarBarrasByProyecto(@PathVariable("ref") String referencia, @PathVariable("emi") Integer emisor) throws IOException {
                String cadenaConsumo = "";
                List<Barras> auxBarras = new ArrayList<>();
                Boolean crearFicheroProcesar = Boolean.FALSE;
		try {
                    cadenaConsumo = barraServices.mochilificarBarrasByProyecto(referencia, emisor);
                    switch (emisor){
                        case 0: // Dise�o, siempre genera los ficheros de LAC si el pedido es para lacar y esos no tienen demora, no llevan TMP, lo llevaran sus ficheros de corte. Se generan pero pasan a la carpeta ESPERA_PROCES
                            
                            break;
                        case 1: // Excell, siempre genera fichero de procesa, ya que es una llamada que se hara para activar el procesamiento de los proyectos. ya sean en stock o porque ha llegado la pintura y activamos el proceso de corte
                            crearFicheroProcesar = Boolean.TRUE;
                            break;
                        case 2: // Forzado, este se usa solo desde vscode para pruebas.
                            break;
                    }
                    if (cadenaConsumo!=null && !cadenaConsumo.contains("No se mochilifica por este emisor.")){
                        auxBarras.addAll(barraServices.getBarrasByProyecto(referencia));
                        GeneradorXML generador = new GeneradorXML(barraServices,stockBarraServices);
                        generador.guardarArchivo(referencia, Integer.valueOf(auxBarras.get(0).getIdentificadorUnicoBarra().substring(6,7)), false);
                        if (crearFicheroProcesar){
                            new FileUtil().registraFicheroProcesar(referencia);
                        } 
                        return new Status(1, "barras del proyecto " + referencia +", mochilificadas correctamente! . Consumo: "+cadenaConsumo);
                    } else {
                        if (crearFicheroProcesar){
                            new FileUtil().registraFicheroProcesar(referencia);
                        }
                        return new Status(1, "Proyecto." + referencia +". Consumo: "+cadenaConsumo);
                    }    
		} catch (Exception e) {
                    if (emisor!=null && emisor.equals(1)){ // excell
                        new FileUtil().registraFicheroProcesar(referencia);
                        return new Status(1, referencia + " Se genera fichero de proceso aunque no hay barras");
                    }    else { 
                        return new Status(0, e.toString());
                    }    
		}

	}
        
        @RequestMapping(value = "proyectos/{ref}/generaFicheroCorte/", method = RequestMethod.GET) 
	public @ResponseBody
	Status generaFicheroCorte(@PathVariable("ref") String referencia) {
                List<Barras> auxBarras = new ArrayList<>();
		try {
                    auxBarras.addAll(barraServices.getBarrasByProyecto(referencia));
                    Boolean estaMochilificado = Boolean.FALSE;  // lo uso para saber si se ha guardado la mochilificacion en base de datos o si estan como pendientes de mochilificar
                    if (auxBarras!=null && !auxBarras.isEmpty()){
                        if (!auxBarras.get(0).getMochilificada().equals(0)){
                            estaMochilificado = Boolean.TRUE;
                        } 
                    }
                    if (estaMochilificado){
                        GeneradorXML generador = new GeneradorXML(barraServices,stockBarraServices);
                        generador.guardarArchivo(referencia, Integer.valueOf(auxBarras.get(0).getIdentificadorUnicoBarra().substring(6,7)), false);
                        return new Status(1, "Fichero de corte generado " + referencia);
                    } else {
                        GeneradorXML generador = new GeneradorXML(barraServices,stockBarraServices);
                        generador.guardarArchivo(referencia, Integer.valueOf(auxBarras.get(0).getIdentificadorUnicoBarra().substring(6,7)), true);
                        return new Status(1, "Fichero de corte generado (desde fichero de txt de barras) " + referencia);
                    }    
		} catch (Exception e) {
                    return new Status(0, e.toString());
		}

	}
        
        
        @RequestMapping(value = "inventario/stockbarras/", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody 
        String	cadenaInventarioStockBarras() {
		try {
                    return stockBarraServices.cadenaInventarioStockBarras();                    
		} catch (Exception e) {                  
                    e.printStackTrace();					
		}
                return "Contacte con un administrador. No es posible calcular inventario stockbarras.";
	}
        
        @RequestMapping(value = "proyectos/{ref}/agregarestos/{codigoactividad}", method = RequestMethod.GET) 
	public @ResponseBody
	Status agregaRestosByProyecto(@PathVariable("ref") String referencia, @PathVariable("codigoactividad") Integer codigoActividad1) {
            String log = "";
		try {
                    Proyectos p = proyectoServices.getProyectoByReferencia(referencia);
                                        
                    log = barraServices.agregaRestosByProyecto(referencia, automantenimiento, codigoActividad1);
                    p.setFecha_corte(new Date());
                    proyectoServices.updateProyecto(p);
                    return new Status(1, log+", Barras eliminadas del proyecto.");                        
                    
                    
                    
                } catch (Exception e) {
                    return new Status(0, e.toString());
		}

	}
        
        @RequestMapping(value = "proyectos/{ref}/liberabarras/", method = RequestMethod.POST) //TODO: investsigar para utilizar DELETE
	public @ResponseBody
	Status liberaBarrasByProyecto(@PathVariable("ref") String referencia) {

		try {
                    barraServices.liberaBarrasByProyecto(referencia);
                    return new Status(1, "Liberadas barras de stock que se habian reservado por el proyecto " + referencia);
                } catch (Exception e) {
                    return new Status(0, e.toString());
		}

	}
        
        /**
         * Libera los restos de un proyecto ya mochilificado y prepara las barras para ser mochilificadas de nuevo.
         * @param referencia
         * @return 
         */
        @RequestMapping(value = "proyectos/{ref}/preparaBarras/", method = RequestMethod.POST) //TODO: investsigar para utilizar DELETE
	public @ResponseBody
	Status preparaBarrasByProyecto(@PathVariable("ref") String referencia) {

		try {
                    barraServices.preparaBarrasByProyecto(referencia);
                    return new Status(1, "Liberadas barras de stock que se habian reservado por el proyecto, y preparadas barras para ser remochilificadas " + referencia);
                } catch (Exception e) {
                    return new Status(0, e.toString());
		}

	}
        
        /**
         * BARRAS STOCK
         */
        @RequestMapping(value = "stockbarras", method = RequestMethod.GET)
	public @ResponseBody List<StockBarras> getStockBarras() {

		List<StockBarras> stockBarras = null ;
		try {
			stockBarras =  stockBarraServices.getStockBarras();   //proyectoServices.getProyectos() ;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return stockBarras;
	}
        
        //Migraci�n de grupo de barra, Crear barra, eliminar barra.
        //NOTA: Se entiende que la longitud de la barra origen y destino no pueden ser la misma en la migraci�n.
        //No permite bajar de 0 las unidades. Esto permite que si se ha encontrado un desajuste de stock, el propio sistema pueda adaptarse de nuevo sin intervenci�n manual.
        @RequestMapping(value = "stockbarras/gestor/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Status gestorStockBarras(@RequestBody StockBarrasAndroid barraAndroid) {
                List<StockBarras> listadoBDD;
		try { 
                    if(barraAndroid.getLongitudOrigen()!=0 && barraAndroid.getLongitudDestino()!=0){
                        if(!barraAndroid.getLongitudOrigen().equals(barraAndroid.getLongitudDestino())){
                            Double mermaSiVamosAMenor = (barraAndroid.getLongitudOrigen()>barraAndroid.getLongitudDestino() ? barraAndroid.getLongitudOrigen() - barraAndroid.getLongitudDestino() : 0);
                            Double restoSiVamosAMayor = (barraAndroid.getLongitudOrigen()<barraAndroid.getLongitudDestino() ? barraAndroid.getLongitudDestino() - barraAndroid.getLongitudOrigen() : 0);
                            listadoBDD = stockBarraServices.getStockBarras(barraAndroid.getColor(), barraAndroid.getTipoSistema(), barraAndroid.getTipoPerfil());
                            for(StockBarras b : listadoBDD){
                                if(b.getLongitud().equals(barraAndroid.getLongitudOrigen()) && b.getUnidades()>0){
                                    b.setUnidades(b.getUnidades() - barraAndroid.getUnidades());
                                    stockBarraServices.deleteBarra(b);
                                    
                                }else if(b.getLongitud().equals(barraAndroid.getLongitudDestino())){
                                    b.setUnidades(b.getUnidades()+barraAndroid.getUnidades());
                                    stockBarraServices.addBarra(b, true);
                                    //public LogBarra(String perfil, String color, Integer longitud, Integer longitudMochilificada, Integer unidadesBarrasMochi, Integer merma, Integer resto, Boolean obligadaStock,Timestamp registro, Timestamp procesado,String proyecto) {
                                    logBarraServices.addLogBarra(new LogBarra(b.getTipoPerfil(),b.getColor(),b.getLongitud().intValue(),0,0,mermaSiVamosAMenor.intValue(),restoSiVamosAMayor.intValue(),false,new Timestamp(Calendar.getInstance().getTimeInMillis()),new Timestamp(Calendar.getInstance().getTimeInMillis()),"MOV-M"));
                                }
                            }
                            return new Status(1, "Barra migrada correctamente!");
                        }else{
                            return new Status(0, "Error en la migraci�n de barra, ambas longitudes iguales!");
                        }
                    }else if(barraAndroid.getLongitudDestino()!=0){
                        listadoBDD = stockBarraServices.getStockBarras(barraAndroid.getColor(), barraAndroid.getTipoSistema(), barraAndroid.getTipoPerfil());
                        for(StockBarras b : listadoBDD){
                            if(b.getLongitud().equals(barraAndroid.getLongitudDestino())){
                                b.setUnidades(b.getUnidades()+barraAndroid.getUnidades());
                                stockBarraServices.addBarra(b, true);
                                // TODO A�ADIR LOG DE UNA BARRA AGREGADA
                                logBarraServices.addLogBarra(new LogBarra(b.getTipoPerfil(),b.getColor(),0,0,0,0,b.getLongitud().intValue(),false,new Timestamp(Calendar.getInstance().getTimeInMillis()),new Timestamp(Calendar.getInstance().getTimeInMillis()),"MOV-A"));
                                
                                return new Status(1, "Barra agregada correctamente!");
                            }
                        }
                        return new Status(0, "Error, no ha sido posible agregar la barra: " + barraAndroid.toString());
                    }else{                        
                        listadoBDD = stockBarraServices.getStockBarras(barraAndroid.getColor(), barraAndroid.getTipoSistema(), barraAndroid.getTipoPerfil());
                        for(StockBarras b : listadoBDD){
                            if(b.getLongitud().equals(barraAndroid.getLongitudOrigen()) && b.getUnidades()>0){
                                b.setUnidades(b.getUnidades() - barraAndroid.getUnidades());
                                stockBarraServices.deleteBarra(b);
                                // TODO A�ADIR LOG DE UNA BARRA ELIMINADA
                                logBarraServices.addLogBarra(new LogBarra(b.getTipoPerfil(),b.getColor(),b.getLongitud().intValue(),0,0,b.getLongitud().intValue(),0,false,new Timestamp(Calendar.getInstance().getTimeInMillis()),new Timestamp(Calendar.getInstance().getTimeInMillis()),"MOV-B"));
                                return new Status(1, "Barra eliminada correctamente!");
                            }
                        }
                        return new Status(0, "Error, puede que no dispongas de existencias o exista un problema en la configuraci�n barra: " + barraAndroid.toString());                        
                    }
			
		} catch (Exception e) {
			// e.printStackTrace();
                    return new Status(0, e.toString());
		}
	}
        @Deprecated //Dejar� de utilizarse para utilizar el stockbarras/gestor 29/04/2019
        @RequestMapping(value = "stockbarras/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Status addStockBarras(@RequestBody StockBarras barra) {
		try {
                    barra.setUnidades(barra.getUnidades() + stockBarraServices.getBarraById(barra.getId()).getUnidades());
                    stockBarraServices.addBarra(barra, false);
                    return new Status(1, "Barra agregada correctamente!");
			
		} catch (Exception e) {
			// e.printStackTrace();
                    return new Status(0, e.toString());
		}
	}
        
        @Deprecated //Dejar� de utilizarse para utilizar el stockbarras/gestor  29/04/2019
        @RequestMapping(value = "stockbarras/{id}/delete", method = RequestMethod.POST) //TODO: investsigar para utilizar DELETE
	public @ResponseBody
	Status deleteStockBarras(@PathVariable("id") long id) {

		try {
                    stockBarraServices.deleteBarra(id);
                    return new Status(1, "barra eliminada correctamente");
		} catch (Exception e) {
                    return new Status(0, e.toString());
		}

	}
        
        @RequestMapping(value = "stockbarras/{id}", method = RequestMethod.GET)
	public @ResponseBody
	StockBarras getStockBarraById(@PathVariable("id") long id) {

		StockBarras barra = null;
		try {
                    barra = stockBarraServices.getBarraById(id);
                        
		} catch (Exception e) {
			e.printStackTrace();
		}
		return barra;
	}
        
        
        /**
         * URI REST EVENTOS    --------------------------------------------
         */
        @RequestMapping(value = "eventos/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Status addEvento(@RequestBody Eventos evento) {
		try {
                        
                        evento.setRegistro(new Timestamp(Calendar.getInstance().getTimeInMillis()));                                                       
			eventoServices.addEvento(evento);                        
			return new Status(1, "evento agregado correctamente!");
		} catch (Exception e) {
			e.printStackTrace();
			return new Status(0, e.toString());
		}
	}        
       
        @RequestMapping(value = "eventos/{id}", method = RequestMethod.GET)
	public @ResponseBody
	Eventos getEventoById(@PathVariable("id") long id) {
                Eventos evento = null;
		try {			
			evento = eventoServices.getEventoById(id);                        
		} catch (Exception e) {
			e.printStackTrace();			
		}
                return evento;
	}
        
        @RequestMapping(value = "eventos/", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Status updateEvento(@RequestBody Eventos evento) {
		try {
			eventoServices.updateEventos(evento);
			return new Status(1, "Evento actualizado correctamente!");
		} catch (Exception e) {
			// e.printStackTrace();
			return new Status(0, e.toString());
		}
	}
        
        /*
            OBTENER LISTADO DE EVENTOS SEG�N LA ACTIVIDAD, OK
        */
        @RequestMapping(value = "eventos/actividades/{id}", method = RequestMethod.GET)
	public @ResponseBody
	List<Eventos> getEventoByActividad(@PathVariable("id") long id) {
		List<Eventos> eventos = null;
		try {
			eventos = eventoServices.getEventosByActividad(id);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return eventos;
	}
        /*
            TODOS LOS EVENTOS DE UNA ACTIVIDAD REALIZADA POR UN USUARIO, OK
        */
        @RequestMapping(value = "eventos/actividades/{id}/usuarios/{usuario}", method = RequestMethod.GET)
	public @ResponseBody
	List<Eventos> getEventoByActividad(@PathVariable("id") long id, @PathVariable("usuario") String nombre) {
		List<Eventos> evento = null;
		try {
			evento = eventoServices.getEventosByActividadUsuario(id,nombre);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return evento;
	}
        /*
            Los eventos de un usuario.
        NOTA: Actualmente utilizamos este m�todo para encontrar el �ltimo evento porque 
        android nos devuelve un error al encontrar un �nico elemento (devolvemos una lista)
        */
        @RequestMapping(value = "eventos/usuarios/{id}", method = RequestMethod.GET)
	public @ResponseBody
	List<Eventos> getEventosByUsuarioId(@PathVariable("id") long id) {
		List<Eventos> evento = null;
		try {
			evento = eventoServices.getEventosByUsuarioId(id);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return evento;
	}
        
        
        /**
         * URI REST ROLES   -------------------------------------------
         */
        @RequestMapping(value = "roles/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Status addRol(@RequestBody Roles rol) {
		try {
			rolServices.addRol(rol);
			return new Status(1, "Rol agregado correctamente!");
		} catch (Exception e) {
			// e.printStackTrace();
			return new Status(0, e.toString());
		}

	}
        @RequestMapping(value = "roles/{id}", method = RequestMethod.GET)
	public @ResponseBody
	Roles getRol(@PathVariable("id") long id) {
		Roles rol = null;
		try {
			rol = rolServices.getRolById(id);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return rol;
	}
        

	@RequestMapping(value = "roles", method = RequestMethod.GET)
	public @ResponseBody
	List<Roles> getRoles() {

		List<Roles> roles = null;
		try {
			roles = rolServices.getRoles();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return roles;
	}
        
        /**
         * URI REST USUARIOS    --------------------------------------------------------------
         */
        @RequestMapping(value = "usuarios", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Status addUsuario(@RequestBody Usuarios usuario) {
		try {
			usuarioServices.addUsuario(usuario);
			return new Status(1, "Usuario agregado correctamente!");
		} catch (Exception e) {
			// e.printStackTrace();
			return new Status(0, e.toString());
		}

	}

	@RequestMapping(value = "usuarios/{usuario}", method = RequestMethod.GET)
	public @ResponseBody
	Usuarios getUsuarioByNombre(@PathVariable("usuario") String nombre) {
		Usuarios usuario = null;
		try {
			usuario = usuarioServices.getUsuarioByNombre(nombre);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return usuario;
	}
        

	@RequestMapping(value = "usuarios", method = RequestMethod.GET)
	public @ResponseBody
	List<Usuarios> getUsuarios() {

		List<Usuarios> usuarios = null;
		try {
			usuarios = usuarioServices.getUsuarios();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return usuarios;
	}

        /**
         * Preparar el proyecto para la llegada de nuevas barras.
         * 1. Recupera las barras de stock utilizadas (si no est�n cortadas)
         * 2. Borrar las barras del proyecto (tanto las necesidades como los restos)
         * 3. No debe de quedar ninguna barra del proyecto para la nueva llegada de barras
         * @param proyecto 
         */
	private void adaptarProyectoParaSincronizarNuevasBarras(Proyectos proyecto) throws Exception{
            if(proyecto.getBarras()!=null && proyecto.getBarras().size()>0){
                
                barraServices.liberaBarrasByProyecto(proyecto.getReferencia());
            }
        }
        
        /**
         * Bloqueamos la sincronizaci�n del proyecto cuando se encuentra la operaci�n de corte en marcha.
         * @param p
         * @return 
         */
        private Boolean bloqueoProyecto(Proyectos p){
            return Boolean.FALSE;
            
            /*if(p!=null){
                for(Actividades act : p.getActividades()){  //Comenzado o finalizado la operaci�n de CORTE
                    if(act.getCodigo().equals(10) && act.getEventos()!=null && act.getEventos().size()>0){
                        return Boolean.TRUE;
                    }
                }
            }*/
            
        }
     
}
