/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilidades; // HOla cambio para push

import com.todocristal.fabrica.webservice.auxiliar.AuxLogger;
import com.todocristal.fabrica.webservice.model.Barras;
import com.todocristal.fabrica.webservice.model.LogBarra;
import com.todocristal.fabrica.webservice.model.Mochilificacion;
import com.todocristal.fabrica.webservice.model.StockBarras;
import com.todocristal.fabrica.webservice.services.BarraServices;
import com.todocristal.fabrica.webservice.services.StockBarrasServices;
import com.todocristal.fabrica.webservice.services.LogBarraServices;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extraido del excel facilitado por Jorge 'Lacador'
 * @author rafael
 */
public class Mochilificador {
    
    private String color;   
    private String tipoSistema;
    private Double MERMAMAX = 500.0;  // TAMAÑO DEL GRUPO
    private Double MERMAACEPTABLEBAJARSTOCK = 300.0; // Si con un corte de una barra tenemos una merma menor a esta y hay stock alto de barras cogemos ese corte. Se inicializa aqui pero puede cambiar segun este documentada en la base de datos
    private Integer STOCKACEPTABLE = 10; // Stock de barras de un grupo por encima del cual aceptaremos coger MERMAACEPTABLEBAJARSTOCK. Se inicializa aqui pero puede cambiar segun este documentada en la base de datos
    private Integer MINIMOUTILIZABLE = 1000;  // POR DEBAJO DE ESTA CANTIDAD LO CONSIDERAMOS RESTO DESECHABLE
    private Integer RESTODESEADO = 2000;  // SERVIRA PARA MOCHILIFICAR CON OBJETIVO OBTERNER BARRAS DE ESTA LONGITUD.
    private Integer DESPUNTE = 20;
    private Integer MARGENCORTE = 10;  //CONSULTAR CON JORGE
    private Integer MARGENLACADO = 100; //CONSULTAR CON JORGE
    private Double BARRALACADOPERFILES = 6100.0;
    private Double ULTIMABARRALACADOPERFILES = 2950.0; //LOGICA PARA DIVIDIR LA ULTIMA BARRA EN DOS TROZOS QUE SEAN MENORES DE 2950 (barra completa/2 = 3150 - 2*MARGENLACADO)
    private List<Barras> listaBarrasNecesidad;
    private List<Barras> auxBarrasEditadas;
    private List<Barras> auxOrdenadoNecesidad;
    private List<Barras> auxNecesidadNoMochilificable;
    private List<Barras> auxNecesidadOtrasBarrasNoMochilificable; // Barras que no sean perfiles por ejemplo el PVROD22 y el PXROD22 y las correderedas tambien
    private List<StockBarras> listaBarrasStock; //Contiene TODAS las barras dado un color y tipo de sistema.
    private StockBarrasServices stockBarrasServices;
    private BarraServices barraServices;
    private AuxLogger logger;
    private ArrayList<AuxLogger> listaLog = new ArrayList<>();
    private MailUtil mail = new MailUtil();
    private String referenciaProyecto = "";
    private Boolean paraLacar;  //Indica si en el proyecto se utiliza barras de CRUDO. (Hay que enviar barras a lacar)
    private String logLacado; // Para adjuntar al email que se envia para lacar
    private String logLacadoPerfiles; // Es especifico para los perfiles, nos sirve para unificar cortes calculados aqui con el log que envia por email.
    private List<String> coloresStock = null;
    private List<String> perfilesMochilificables = null;
    private List<String> otrasBarrasNoMochilificables = null; // aqui pondremos barras que no se mochilifican pero que tampoco son perfiles de paneles (tipo el perf10 o el perod10 que tienen su propio tratamiento para intentar cuadrar bien sus cortes
    // aqui iran por ejemplo los PVERT22 y PEXT22 o algunos de corredera.
    private String logResultadoTotal = "";
    private String colorAdaptado = "";
    private String lacadoForzado = "";
    private String grosorPerfil = "";
    private String cadenaConsumo = "";
    private Date fechaLog = new Date(); // Usamos la fecha estandar para el log restos
    private Double totalPerfilesConsumidos = 0.0;
    private ArrayList<Integer> seccionesEditadas = null;
    private Boolean contienePLToPU;
    private Boolean contieneOtrasBarrasNoMochilificables;
    private Boolean barraObligadaPorStock; // Se usa para forzar que el corte se haga de una barra de grupo en concreto por bajar stock de ese grupo.
    private Boolean barraObligadaCompleta;
    private ArrayList<Mochilificacion> mochilificaciones;
    private List<Barras> auxRestosBarraNueva = new ArrayList<>();  
    private List<StockBarras> auxBarrasStockLacar = new ArrayList<>();
    private List<Double> auxBarrasStockLacarNecesidad = new ArrayList<>();
    private List<StockBarras> auxBarrasStockColorStock = new ArrayList<>();
    private List<Double> auxBarrasStockNecesidad = new ArrayList<>();
    private Integer emisor;
    private LogBarraServices logBarraServices;
    private String otros_lacables_proyecto;
    private Double longitudBarraCompletaSistema;
    
    
    public Mochilificador(List<Barras> listaNecesidades, StockBarrasServices stockBarrasServices, BarraServices barraServices, Boolean variosColores, Integer emisor, LogBarraServices logBarraServices
    , String otros_lacables) throws Exception{
        this.listaBarrasNecesidad = listaNecesidades;        
        //MERMAMAX = 300.0;
        this.stockBarrasServices = stockBarrasServices;
        this.barraServices = barraServices;
        this.perfilesMochilificables = barraServices.inicializarPerfilesMochilificables();
        this.otrasBarrasNoMochilificables = barraServices.inicializarOtrasBarrasNoMochilificables();
        this.color = barraServices.colorAluminio(this.listaBarrasNecesidad);
        this.grosorPerfil = barraServices.grosorPerfil(this.listaBarrasNecesidad);
        this.lacadoForzado = barraServices.comprobarSiHayLacadoForzado(listaNecesidades);
        this.tipoSistema = listaNecesidades.get(0).getTipoSistema()!=null ? listaNecesidades.get(0).getTipoSistema() : ""; // Las barras que se mochilifican son comunes a status y cruiser, pero estan definidas como status
        this.coloresStock = barraServices.inicializarColores(this.tipoSistema, (this.grosorPerfil!=null && this.grosorPerfil.equals("PERFIL10")));
        this.paraLacar = barraServices.paraLacar(this.color, this.grosorPerfil, this.lacadoForzado, coloresStock);
        this.referenciaProyecto = listaNecesidades!=null ? listaNecesidades.get(0).getIdentificadorUnicoBarra().substring(0, 5): "00000";
        this.logResultadoTotal = listaNecesidades!=null ? listaNecesidades.get(0).getTagA(): "Sin referencia";
        this.longitudBarraCompletaSistema = listaNecesidades!=null && listaNecesidades.get(0).getTipoSistema().contains("INFINIA") ? 7000.0 : 6300.0;
        this.colorAdaptado = barraServices.adaptaColorALacado(this.color, this.lacadoForzado, this.paraLacar);
        this.logLacado="";
        this.logLacadoPerfiles="";
        this.auxBarrasEditadas= new ArrayList<>();
        this.auxOrdenadoNecesidad = new ArrayList<>();
        this.seccionesEditadas = new ArrayList<>();
        this.contienePLToPU = Boolean.FALSE;
        this.contieneOtrasBarrasNoMochilificables = Boolean.FALSE;
        this.auxNecesidadNoMochilificable = new ArrayList<>();
        this.barraObligadaPorStock = Boolean.FALSE;
        this.barraObligadaCompleta = Boolean.FALSE;
        this.mochilificaciones = new ArrayList<>();
        this.auxRestosBarraNueva = new ArrayList<>();
        this.auxBarrasStockLacar = new ArrayList<>();
        this.auxBarrasStockLacarNecesidad = new ArrayList<>();
        this.auxBarrasStockColorStock = new ArrayList<>();
        this.auxBarrasStockNecesidad = new ArrayList<>();
        this.emisor = emisor; // Emisor 0, diseño, se mochilifica solo si es para lacar. Emisor 1 excel, se mochilifica solo si es color stock. Emisor 2. Se mochilifica ya sea para lacar o stock. (opcion antigua)
        this.logBarraServices = logBarraServices;
        this.otros_lacables_proyecto = otros_lacables;
        
    }
    
    public String ejecutar() {

        if ((emisor == 2) || (paraLacar && emisor == 0) || (!paraLacar && emisor == 1)) {

            String logMovimientosStockBarra = "";

            try {
                // Cada vez que por alguna razon se mochilifica de nuevo, hay que borrar los logBarras de previas mochilificaciones. // TODO VER QUE PASA SI HAY DOS COLORES
                List<LogBarra> logBarrasProyecto = new ArrayList<>();
                logBarrasProyecto = logBarraServices.getLogBarrasByProyecto(referenciaProyecto);
                if (!logBarrasProyecto.isEmpty()) {
                    logBarraServices.deleteLogBarrasByProyecto(referenciaProyecto);
                }

                //TODO analizar si existen barras de otras versiones, por error. En al addBarra podria hacerse?
                analizaSiHayBarrasConDosVersiones();

                //Ordenar listado LBN + CARRIL-COM-PER
                ordenarYAgrupaBarrasNecesidad();

                //Buscar barra en stock para cada una de las barras
                listaBarrasStock = stockBarrasServices.getStockBarras(colorAdaptado, tipoSistema);

                //Creamos un nuevo grupo de stockbarras barras dado el color en el caso de ser necesario.
                if (listaBarrasStock == null || listaBarrasStock.isEmpty()) {
                    stockBarrasServices.agregarNuevoGrupoDadoGrupoBase(colorAdaptado, stockBarrasServices.getStockBarras("CRUDO", tipoSistema));
                    listaBarrasStock = stockBarrasServices.getStockBarras(colorAdaptado, tipoSistema);
                }

                rellenaMochilificaciones(auxOrdenadoNecesidad);

                stockBarrasServices.updateBarras(listaBarrasStock);

                // Una vez mochilificadas las barras que se pueden mochilificar, le agrego las barras no mochilificables tal y como vienen de appglass
                auxOrdenadoNecesidad.addAll(auxNecesidadNoMochilificable);
                //auxOrdenadoNecesidad.addAll(auxNecesidadOtrasBarrasNoMochilificable);
                new FileUtil().logBarrasMochilificadasAtxt(referenciaProyecto, auxOrdenadoNecesidad, false);

                barraServices.updateBarras(auxOrdenadoNecesidad);
                for (Barras b : auxRestosBarraNueva) {
                    barraServices.addBarra(b);
                }

                if (!logMovimientosStockBarra.isEmpty()) {
                    //new FileUtil().registrarLogMovimientos(colorAdaptado, logMovimientosStockBarra);
                }
                mostrarLog(listaLog, emisor);
                if (paraLacar) {
                    if (auxBarrasStockLacar != null) {
                        if (!auxBarrasStockLacar.isEmpty()) {
                            StockBarras auxBarraStock = calcularLongitudPerfiles(auxOrdenadoNecesidad);
                            if (auxBarraStock != null) {
                                auxBarrasStockLacar.add(auxBarraStock);
                                auxBarrasStockLacarNecesidad.add(auxBarraStock.getLongitud());
                            }
                        }
                        if (contieneOtrasBarrasNoMochilificables!=null && contieneOtrasBarrasNoMochilificables){
                            ArrayList<StockBarras> auxOtrasBarrasStock = new ArrayList<>();
                            auxOtrasBarrasStock.addAll(calcularLongitudOtrasBarras(auxOrdenadoNecesidad));
                            if (!auxOtrasBarrasStock.isEmpty()) {
                                for (StockBarras sb : auxOtrasBarrasStock) {
                                    auxBarrasStockLacar.add(sb);
                                    auxBarrasStockLacarNecesidad.add(sb.getLongitud());
                                }
                            }
                        }
                        // TODO generar cadena que se enviara en el mail.notificarBarrasParaLacar en vez de hacerlo alli y ademas genera cadena de consumo para enviarla como resultado a appglass
                        cadenaConsumo = generaCadenaConsumo(auxBarrasStockLacar, auxBarrasStockLacarNecesidad);
                        if (contienePLToPU || contieneOtrasBarrasNoMochilificables) {  // No modificar este texto, se usa como bandera para sacar un mensaje en el email de lacado
                            logLacado += "<br><b>NOTA: El pedido contiene barras no mochilificables, por favor, REVISAD</b><br>";
                        }
                        
                        mail.notificarBarrasParaLacar(auxBarrasStockLacar, auxBarrasStockLacarNecesidad, referenciaProyecto, this.color, logLacado, logLacadoPerfiles,
                                (seccionesEditadas != null && !seccionesEditadas.isEmpty() ? seccionesEditadas.toString() : ""), lacadoForzado, otrasBarrasNoMochilificables, cadenaConsumo, otros_lacables_proyecto);
                    }

                } else {  //no es para lacar, aun asi usamos el metodo de calculo de perfiles para generar el consumo
                    auxBarrasStockColorStock.add(calcularLongitudPerfiles(auxOrdenadoNecesidad));
                    if (contieneOtrasBarrasNoMochilificables){
                        auxBarrasStockColorStock.addAll(calcularLongitudOtrasBarras(auxOrdenadoNecesidad));
                    }    
                    auxBarrasStockNecesidad.add(auxBarrasStockColorStock.get(auxBarrasStockColorStock.size() - 1).getLongitud());
                    cadenaConsumo = generaCadenaConsumo(auxBarrasStockColorStock, auxBarrasStockNecesidad);
                }
                return cadenaConsumo;

            } catch (Exception ex) {
                System.out.println("Error durante la ejecucion del mochilificador: " + ex.toString());
                return "ERROR";
            }

        } else { // NO CAMBTAR NUNCA ESTA STRING SE CONTROLA EN DISEÑO
            return "No se mochilifica por este emisor " + emisor + " (0 diseño, 1 excel). Lacado " + (paraLacar ? "SI" : "NO")+ ". Solo iniciamo proceso";
        }

        
        
    }
    
    
    
    //--------------------------------------------------------------
    /**
     * Busca una barra del stock para relacionar con la barra necesidad.     
     * @param barraNecesidad
     * @param listadoGrupoCompatibles Dispone de los grupos compatibles con color y tipo de cortina.
     * @return 
     */
    private StockBarras buscarBarraCompatibleEnStock(Double longitudBarraNecesidad, String tipoPerfil, List<StockBarras> listadoGrupoCompatibles) throws Exception {
        StockBarras barraCompatible = null;
        StockBarras barraCompleta = null;
        String tipoPerfilAdaptado = (tipoPerfil.equals("CARRIL") || tipoPerfil.equals("CARRIL_INV") ? "CARRIL" : (tipoPerfil.startsWith("CARRILPLUS") ? "CARRILPLUS" : tipoPerfil));
        barraObligadaPorStock = Boolean.FALSE;
        barraObligadaCompleta = lacadoForzado.equals("ENTERAS") || listadoGrupoCompatibles.get(0).getColor().equals("CRUSOP");
        

        // Recorremos las barras que son compatibles con la nuestra y de todas ellas seleccionamos por si nos hace falta, la barra de barra completa, y la barra mas proxima por grupo (barracompatible) a nuestra barraNecesidad, 
        for (StockBarras stockBarra : listadoGrupoCompatibles) {
            if (stockBarra.getLongitud().equals(longitudBarraCompletaSistema) && stockBarra.getTipoPerfil().equals(tipoPerfilAdaptado)) {
                barraCompleta = stockBarra;
            }
            if (stockBarra.getLongitud() >= longitudBarraNecesidad && stockBarra.getTipoPerfil().equals(tipoPerfilAdaptado) && stockBarra.getUnidades() > 0) {
                if (compararMerma(barraCompatible, stockBarra, longitudBarraNecesidad)) {
                    barraCompatible = stockBarra;
                }
            }
        }
        // Una vez tenemos ya las dos StockBarras que podemos necesitar, puede darse los siguientes casos
        // A) estamos obligados a enviar como barra para mochilificar la barra de barra completa ya que es un proyecto de lacado forzado barras enteras o a sopena. (barraObligadaCompleta)
        // B) la barra compatible es de barra completa, no hay que comprobar nada mas.
        // C) la barra compatible tiene muchas unidades en stock, por lo que aceptamos perder una merma algo alta, para reducir el stock de ese grupo (barraObligadaStock)
        // D) Una vez hemos pasado las barras obligadas, comparamos la merma que produciria nuesta barraCompatible, respecto a la merma que quedaria si la cortamos de una barra de barra completa y la asignamos a un grupo, en caso de ser menor la merma
        // de la barra compatible, devolvemos esta
        // E) el else del caso D, es decir, es mejor cortar de una barra de barra completa para que el resto, una vez agrupado nos produzca una merma menor que si hubieramos cortado de la barraCompatible.
        // G) Puede darse el caso de que no encontremos barra compatible pero sea porque por pocos milimetros se sobrepase los barra completa esto solo puede darse hasta 6320 en el peor de los casos en los que el carril mida 6099 y el compensador
        // por llevar el sistema cierre lateral mida 6119 y al aplicar el extra lacado el compensador llegue a 6319, pero esto se puede hacer con una barra de barra completa .. ver proyecto 53972
        // F) por un error en la asignacion de barras, devolvemos un null para provocar error.

        if (barraObligadaCompleta) { //A.
            return barraCompleta; // Forzamos que mochilifique con barra completa
        } else {
            if (barraCompatible != null) {
                //Si la barra compatible tiene informados los valores de mermaForzar y stockForzar, cambio los valores de las variables, si no, dejo los valores por defecto.
                MERMAACEPTABLEBAJARSTOCK = (barraCompatible.getMermaForzar() != null ? barraCompatible.getMermaForzar() : MERMAACEPTABLEBAJARSTOCK);
                STOCKACEPTABLE = (barraCompatible.getStockForzar() != null ? barraCompatible.getStockForzar() : STOCKACEPTABLE);
                if (barraCompatible.getLongitud().equals(longitudBarraCompletaSistema)) {  //B. la barra elegida es barra completa por las propiedades del corte en si mismo
                    return barraCompatible;
                } else if (((barraCompatible.getLongitud() - longitudBarraNecesidad) < MERMAACEPTABLEBAJARSTOCK) && barraCompatible.getUnidades() >= STOCKACEPTABLE) { //C. Cogemos la barra porque tenemos demasiadas en stock y aceptamos perder merma
                    barraObligadaPorStock = Boolean.TRUE;
                    return barraCompatible;
                } else if ((barraCompatible.getLongitud() - longitudBarraNecesidad) <= mermaGrupoInferiorCorteCompleta(longitudBarraNecesidad)) { //D.
                    return barraCompatible;
                } else { //E.  En este caso es mejor devolver barra de barra completa para que gestione el corte y gnere el resto.
                    return barraCompleta;
                }
            } else if (longitudBarraNecesidad > 5.500) { //F
                //si la barra es mayor que el mayor grupo de stock barras, directamente devuelvo barra completa.
                return barraCompleta;
            } else {
                return null; //G
            }
        }

    }
    
    private boolean compararMerma(StockBarras barraCompatible,StockBarras barraStock, Double longitudBarraNecesidad){
        return barraCompatible==null || (barraStock.getLongitud() - longitudBarraNecesidad < barraCompatible.getLongitud() - longitudBarraNecesidad);
    }
    
    private Double mermaGrupoInferiorCorteCompleta (Double longitudBarraNecesidad){
        Double restoCorteBarraCompleta = longitudBarraCompletaSistema - longitudBarraNecesidad;
        
        return (restoCorteBarraCompleta - Double.valueOf(grupoMenorLongitud(restoCorteBarraCompleta)));
    }
    
    /**
     * TODO: La barra nueva debe ser de la misma longitud para que sea cortada justamente despuï¿½s de la barra necesidad.
     * @param barra (Nos facilita la creación de la barra resto)
     * @param longitudBarraConsumo (consumo necesario real para las barras necesidad) 17/05/2018.
     * @return 
     */
    private Barras crearBarraResto(Barras barra, Integer contadorGrupoStock, Double longitudBarraConsumo){
        Barras aux= new Barras();
        
        
        aux.setLongitud(0.0);//Debe ser pequeña para que siempre la ponga como ultimo corte
        // la longitud de este corte es teorica en realidad solo sirve para ponerle la etiqueta al resto.
        aux.setMerma(longitudBarraCompletaSistema-Double.valueOf(grupoMenorLongitud(longitudBarraCompletaSistema - longitudBarraConsumo)));//Necesario para xml
        aux.setResto(Double.valueOf(grupoMenorLongitud(longitudBarraCompletaSistema - longitudBarraConsumo)));
        aux.setAnguloIzquierdo(0.0); // Se ponen a 0 para que la maquina de error de corte y sepamos que es un resto para imprimir etiqueta
        aux.setAnguloDerecho(0.0);
        aux.setTagA("RESTO:"+ grupoMenorLongitud(longitudBarraCompletaSistema - longitudBarraConsumo));
        aux.setIdentificadorUnicoBarra("RESTO-" + grupoMenorLongitud(longitudBarraCompletaSistema - longitudBarraConsumo));
        aux.setTagB(referenciaProyecto);
        aux.setTagC("");
        aux.setTagD("");
        aux.setColor(colorAdaptado);
        aux.setEditada(barra.getEditada());
        aux.setGrosor(barra.getGrosor());
        aux.setGrupoStock(0);        
        aux.setInvertir(barra.getInvertir());
        aux.setProyecto(barra.getProyecto());
        aux.setTipoPerfil(barra.getTipoPerfil());
        aux.setTipoSistema(barra.getTipoSistema());
        aux.setLongitudExterior(0.0); //OL forzada a 0.0 como prueba
        aux.setLongitudInterior(0.0);
        aux.setPerteneceASeccion(barra.getPerteneceASeccion());
        aux.setMochilificada(1);
        aux.setIdentificador_stock(contadorGrupoStock);
        
        return aux;
    }
    
    private StockBarras crearBarraCompleta(Barras barra){
        StockBarras aux= new StockBarras();
        aux.setLongitud(longitudBarraCompletaSistema);
        aux.setColor(colorAdaptado);
        aux.setTipoPerfil(barra.getTipoPerfil());
        aux.setTipoSistema(barra.getTipoSistema());
        return aux;
    }
    
    /**
     * Encontrar su barra de stock relacionada dados los parámetros de una barra necesidad.
     * @param longitud
     * @param listadoGrupos Listado de stockbarras completo relacionado con la barra necesidad.
     * @return 
     */
    private StockBarras ObtenerGrupoStockRelacionado(Double longitud, String tipoPerfil, List<StockBarras> listadoGrupos){
        //StockBarras grupoSeleccionado = null;
        StockBarras optimaSB=null;
        
        for(StockBarras sb : listadoGrupos){
            if(sb.getTipoPerfil().equals(tipoPerfil)){
                if(optimaSB == null && sb.getLongitud()<= longitud){
                    optimaSB = sb;
                }
                if(optimaSB!=null && sb.getLongitud() <= longitud ){
                    if(sb.getLongitud() > optimaSB.getLongitud()){
                        optimaSB = sb;
                    }                    
                }
            }            
        }        
        
        return optimaSB;
    }
    
    /**
     * Agrega los restos al stockbarras que se hallan generado durante la mochilificación del proyecto 
     * Tanto si existe como sino RESTOS se eliminan las barras para automantenimiento de la tabla. (Inicialmente no se eliminarán en ningún caso para seguimiento)
     * En caso de que no existan barras devuelve resultado FALSE.
     * Actualizo las barras RESTO como AGREGADAS al stock usando el campo POSICION.     
     * @param listaBarras
     * @param automantenimiento True borra las barras necesidad una vez cortado el proyecto.
     * @param codigoActividad   Se pueden agregar restos antes de enviar (2) a lacar y al cortar en la tronzadora.(operacion 10)
     * @return
     * @throws Exception 
     */
    public String agregaRestos(List<Barras> listaBarras, Boolean automantenimiento, Integer codigoActividad) throws Exception{
        //Actualizar grupos de stockbarras
        StockBarras sb = new StockBarras();
        Boolean hayRestosAprovechables = Boolean.FALSE;
        String auxReferencia = (listaBarras!=null && !listaBarras.isEmpty()? 
                (!listaBarras.get(0).getIdentificadorUnicoBarra().substring(0, 5).contains("REST") ? listaBarras.get(0).getIdentificadorUnicoBarra().substring(0, 5): "ZZZZZ") : null); 
        String logRestos = "";
        
        if(listaBarras!=null && (codigoActividad.equals(10) || (codigoActividad.equals(2) && paraLacar )) ){
            listaBarrasStock = stockBarrasServices.getStockBarras(colorAdaptado, listaBarras.get(0).getTipoSistema());
            for (Barras b: listaBarras){
                if (checkCondicionesBarraStock(b)){                    
                    b.setPosicion("AGREGADA");
                    // TODO: Ver con alejandro porque usamos la merma y no el resto.
                    sb = ObtenerGrupoStockRelacionado(b.getResto(),b.getTipoPerfil(), listaBarrasStock); // Usamos la merma como longitud en las barras resto, por necesidad del xml
                    if (sb!=null){
                        listaBarrasStock.get(listaBarrasStock.indexOf(sb)).setUnidades(listaBarrasStock.get(listaBarrasStock.indexOf(sb)).getUnidades()+1);  
                        hayRestosAprovechables = Boolean.TRUE;
                        logRestos +="\n"+ fechaLog.toString()+";"+ auxReferencia +";"+ sb.getTipoSistema()+";"+ sb.getTipoPerfil()+";"+sb.getColor()+";"+sb.getLongitud()+";AGREGA;"+"+1";
                    }    
                }            
            }
            
            if(hayRestosAprovechables){
                barraServices.updateBarras(listaBarras);
                //new FileUtil().registrarLogMovimientos(colorAdaptado, logRestos);
                
                if(listaBarrasStock!=null && !listaBarrasStock.isEmpty())
                    stockBarrasServices.updateBarras(listaBarrasStock);
            }
            
            //Inicialmente dejamos las barras para verificar el funcionamiento.
            if(automantenimiento){
                barraServices.deleteBarrasByProyecto(auxReferencia);
            }else{                
                System.out.print("No se han borrado las barras necesidad por automantenimiento FALSO");
            }
            logBarraServices.updateLogBarras(logBarraServices.getLogBarrasByProyecto(auxReferencia));
            mail.notificarAgregadosRestos(listaBarras.get(0).getIdentificadorUnicoBarra().substring(0, 5), colorAdaptado,hayRestosAprovechables);
            return ("Color: " + colorAdaptado + " Ok! Codigo Actividad:" + codigoActividad + " 2 añade crudo, 10 añade stock");
        }else{
            return ("Color: " + colorAdaptado + " Error! Codigo Actividad:" + codigoActividad + " 2 añade crudo, 10 añade stock");
        }
    
    }
    
    /**
     * Libera las barras stockReservada en un proyecto mochilificado.
     * Borrar barras después de devolver el stock.
     * No añade barras de barra completa
     * @param listaBarras
     * @return
     * @throws Exception 
     */
    public String liberaBarras(List<Barras> listaBarras) throws Exception{
        Integer auxPosicion;
        String auxReferencia = listaBarras.get(0).getIdentificadorUnicoBarra().substring(0, 5);
        String logStockBarras = "";
        //Actualizar grupos de stockbarras
        if(listaBarras!=null){
            listaBarrasStock = stockBarrasServices.getStockBarras(colorAdaptado, listaBarras.get(0).getTipoSistema());
            if(listaBarrasStock!=null && listaBarrasStock.size()>0){
                for (Barras b: listaBarras){
                    if (checkCondicionesLiberaBarraStock(b)){    // Se podria indicar en el metodo check, que si la barra a liberar es una de barra completa no la devuelva al stock                
                        auxPosicion = buscarPosicionBarraStock(b.getGrupoStock(), listaBarrasStock);
                        if(auxPosicion !=null && listaBarrasStock.get(auxPosicion).getLongitud()< longitudBarraCompletaSistema){                            
                            listaBarrasStock.get(auxPosicion).setUnidades(listaBarrasStock.get(auxPosicion).getUnidades()+1);
                            logStockBarras +="\n"+ fechaLog.toString()+";"+ auxReferencia +";"+ listaBarrasStock.get(auxPosicion).getTipoSistema()+";"+ listaBarrasStock.get(auxPosicion).getTipoPerfil()+";"
                                    +listaBarrasStock.get(auxPosicion).getColor()+";"+listaBarrasStock.get(auxPosicion).getLongitud()+";LIBERA;"+"+1";
                        }
                    }        
                }
                stockBarrasServices.updateBarras(listaBarrasStock);
            }            
            
            if (!logStockBarras.isEmpty()){
                //new FileUtil().registrarLogMovimientos(colorAdaptado, logStockBarras);
            }
            mail.notificarLiberadasBarras(listaBarras.get(0).getIdentificadorUnicoBarra().substring(0, 5), barraServices.colorAluminio(listaBarras));
        }
        return "Liberadas barras proyecto ," + referenciaProyecto + " Color:" + colorAdaptado + "["+color+"]";    
    }
    
    public String preparaBarras(List<Barras> listaBarras) throws Exception{
        Integer auxPosicion;
        Integer auxReferencia = (listaBarras!=null && !listaBarras.isEmpty() 
                ? ( !listaBarras.get(0).getIdentificadorUnicoBarra().substring(0, 5).contains("REST") ? Integer.valueOf(listaBarras.get(0).getIdentificadorUnicoBarra().substring(0, 5)):0) : null); // Un cero indica que no hay referencia, ya que estamos en una barra resto
        String logStockBarras = "";
        List<Barras> barrasResto = new ArrayList<>();
        //Actualizar grupos de stockbarras
        if(listaBarras!=null && auxReferencia!=0){
            listaBarrasStock = stockBarrasServices.getStockBarras(colorAdaptado, listaBarras.get(0).getTipoSistema());
            if(listaBarrasStock!=null && listaBarrasStock.size()>0){
                for (Barras b: listaBarras){
                    if (checkCondicionesLiberaBarraStock(b)){    // Se podria indicar en el metodo check, que si la barra a liberar es una de barra completa no la devuelva al stock                
                        auxPosicion = buscarPosicionBarraStock(b.getGrupoStock(), listaBarrasStock);
                        if(auxPosicion !=null && listaBarrasStock.get(auxPosicion).getLongitud()< longitudBarraCompletaSistema){                            
                            listaBarrasStock.get(auxPosicion).setUnidades(listaBarrasStock.get(auxPosicion).getUnidades()+1);
                            logStockBarras +="\n"+ fechaLog.toString()+";"+ auxReferencia +";"+ listaBarrasStock.get(auxPosicion).getTipoSistema()+";"+ listaBarrasStock.get(auxPosicion).getTipoPerfil()+";"
                                    +listaBarrasStock.get(auxPosicion).getColor()+";"+listaBarrasStock.get(auxPosicion).getLongitud()+";LIBERA;"+"+1";
                        }
                    }        
                }
                stockBarrasServices.updateBarras(listaBarrasStock);
            }            
            
            if (!logStockBarras.isEmpty()){
                //new FileUtil().registrarLogMovimientos(colorAdaptado, logStockBarras);
            }
        }
        
        // Una vez devueltas las barras stock liberadas, tenemos que recorrer las barras y poner a cero los campos involucrados en la mochilificacion, asi como eliminar las barras resto.
        for (Barras b: listaBarras){
            if (b.getIdentificadorUnicoBarra().contains("REST")){
                barrasResto.add(b);
            } else {
                b.setMochilificada(0);
                b.setResto(0.0);
                b.setMerma(0.0);
                b.setGrupoStock(0);
                b.setIdentificador_stock(0);
                // Tenemos que eliminar la cadena desde la @ si existe, pero no lo de antes.
                b.setTagD(b.getTagD()!=null ? (!b.getTagD().contains("@")? b.getTagD() : b.getTagD().substring(0,b.getTagD().indexOf("@"))) : null);
            }
        }
        // Ojo los restos que vienen aqui se han cogido con el color del proyecto, por lo que si hay restos crudo (pedido de lacado) no aparecerian aqui y no se eliminarian, asi que tenemos que ver si hay restos del proyecto en crudo.
        if (!barrasResto.isEmpty()){
            listaBarras.removeAll(barrasResto);
        }
        if (!listaBarras.isEmpty()){
            barraServices.updateBarras(listaBarras);
        }    
        if (!barrasResto.isEmpty()){
            barraServices.borraBarras(barrasResto);
        }
        
        
        
        return "Preparadas barras proyecto ," + referenciaProyecto + " Color:" + colorAdaptado + "["+color+"] Ya puedes mochilificar de nuevo";    
    }
    
    /**
     * COmprueba para las barras de tipo RESTO cumple las condiciones para agregarse al stockbarras.
     * A Cumplir: Ser RESTO y ser diferente a tipo PERFIL.
     * @param b
     * @return 
     */
    private Boolean checkCondicionesBarraStock(Barras b){
        return b.getIdentificadorUnicoBarra().contains("REST") 
                && !b.getTipoPerfil().contains("PERF")
                && !b.getTipoPerfil().contains("PEROD")
                && (b.getPosicion() == null || !b.getPosicion().contains("AGREG"));
    }
    
    /**
     * Sólo se devuelven a barras de stock las que no sean PERFIl y esten mochilificadas. Los RESTOS tampoco (17/05/2018).
     * @param b
     * @return 
     */
    private Boolean checkCondicionesLiberaBarraStock (Barras b){
        return (!b.getTipoPerfil().contains("PERF") && !b.getTipoPerfil().contains("PEROD") && !b.getIdentificadorUnicoBarra().contains("RESTO") && b.getMochilificada().equals(1));
    }
    
    private void mostrarLog(ArrayList<AuxLogger> listado, Integer emisor) throws IOException{
        if(listado!=null && !listado.isEmpty()){
            for(AuxLogger log : listado){
                System.out.println("Resultado "+ log.toString());
            }
        //new FileUtil().registrarLogProyecto(listado.get(0).getNomProyecto(), listado, color, logResultadoTotal, emisor);   
        }
    }
    
    
    private Integer buscarPosicionBarraStock(Integer idGrupoStock, List<StockBarras> listaBarrasStock){
        for(int i=0; i< listaBarrasStock.size(); i++){
            if(listaBarrasStock.get(i).getId() == idGrupoStock)
                return i;
        }
        return null;
    }
    
       
    private String grupoMenorLongitud (Double longitud){
        
        Integer auxLong = longitud.intValue();
        String grupo = "0";
        if (auxLong >=1000 && auxLong < 1500){
            grupo = "1000";
        } else if (auxLong >=1500 && auxLong < 2000){
            grupo = "1500";
        } else if (auxLong >=2000 && auxLong < 2500){
            grupo = "2000";
        } else if (auxLong >=2500 && auxLong < 3000){
            grupo = "2500";
        } else if (auxLong >=3000 && auxLong < 3500){
            grupo = "3000";
        } else if (auxLong >=3500 && auxLong < 4000){
            grupo = "3500";
        } else if (auxLong >=4000 && auxLong < 4500){
            grupo = "4000";
        } else if (auxLong >=4500 && auxLong < 5000){
            grupo = "4500";
        } else if (auxLong >=5000 && auxLong < 5500){
            grupo = "5000";
        } else if (auxLong >=5500){
            grupo = "5500";
        }
        
        return grupo;
    }
  
    /* NUEVOS GRUPOS*/
    private Boolean esGrupo (Double longitud){
        ArrayList<Double> grupos = new ArrayList<>();
        grupos.add(1000.0);
        grupos.add(1500.0);
        grupos.add(2000.0);
        grupos.add(2500.0);
        grupos.add(3000.0);
        grupos.add(3500.0);
        grupos.add(4000.0);
        grupos.add(4500.0);
        grupos.add(5000.0);
        grupos.add(5500.0);
        grupos.add(6300.0);
        grupos.add(7000.0);
   
        return (grupos.contains(longitud));
    }
    
    
    private StockBarras calcularLongitudTipoPerfil(List<Barras> auxListado, String tipoPerfil){
        Integer contadorBarrasCompletas = 0;
        Integer contadorBarras3150 = 0;
        Integer contPerfilesBarra = 0;
        Integer contPerfilesSeccionBarra = 0;
        Double resto = 0.0; // Inicializamos mas tarde dependiendo de la lonit
        Integer seccionActual = 1; // Aqui la secciones de las barras ya empiezan en 1
        List<Barras> auxListadoPerfiles = new ArrayList<>();
        List<Barras> auxListadoPerfilesSeccion = new ArrayList<>();
        List<Barras> auxListadoPerfilesOrdenado = new ArrayList<>();
        String tipoSistema = "";
        String auxTipoPerfil = null;
        Double ultimaBarraEscogida = 0.0;
        Boolean esLacadoEnteras = lacadoForzado.equals("ENTERAS") || colorAdaptado.equals("CRUSOP");
        totalPerfilesConsumidos = 0.0;
        Boolean partimosUltimaBarraPerfiles = Boolean.FALSE;

        Double restoDelTotalPerfiles = 0.0;

        //Extraemos solo los perfiles de las barras necesidad y aprovechamos para ver cuantas secciones hay
        if (auxListado != null) {
            tipoSistema = auxListado.get(0).getTipoSistema()!=null ? auxListado.get(0).getTipoSistema() : "";
            for (Barras b : auxListado) {
                if (b.getTipoPerfil().contains(tipoPerfil)) {
                    auxListadoPerfiles.add(b);
                    auxTipoPerfil = b.getTipoPerfil();
                    restoDelTotalPerfiles += b.getLongitud() + MARGENCORTE; // Acumulamos aqui el total de perfiles para poder controlar las barras de crudo que cogemos, por el tema de las ultimas barras cortas.
                    if (b.getPerteneceASeccion() > seccionActual) {
                        seccionActual = b.getPerteneceASeccion();
                    }
                }
            }
            // Una vez separados, los agrupamos por seccion y los ordenamos por longitud usando el sort de Barras
            if (!auxListadoPerfiles.isEmpty()) {
                for (int i = 1; i <= seccionActual; i++) {
                    for (Barras p : auxListadoPerfiles) {
                        if (p.getPerteneceASeccion().equals(i)) {
                            auxListadoPerfilesSeccion.add(p);
                        }
                    }
                    Collections.sort(auxListadoPerfilesSeccion); // al ordernarlo se quedan de menor a mayor por lo que los invertimos justo despues
                    /*for (int ibs=auxListadoPerfilesSeccion.size()-1; ibs>=0; ibs--){
                        auxListadoPerfilesOrdenado.add(auxListadoPerfilesSeccion.get(ibs));
                    }*/
                    auxListadoPerfilesOrdenado.addAll(auxListadoPerfilesSeccion);
                    auxListadoPerfilesSeccion = new ArrayList();
                }
            }
            // Ya tenemos el array de perfiles ordenados por seccion y longitud.
            if (!auxListadoPerfilesOrdenado.isEmpty()) {
                seccionActual = 1;
                String seccionesEnBarra = "";
                //contadorBarrasNuevas = 1;  // Empezamos con una barra nueva
                //logLacadoPerfiles += "B"+contadorBarrasNuevas+"->";
                logLacado += "PERFILES: Comenzamos cortando barras enteras siguiendo el orden de los perfiles del proyecto. Si hay una barra corta, será para el final de la ultima sección.<br>";
                //INICIALIZAMOS LA BARRAS RESTO SEGUN SEA EL TOTAL DE PERFILES, PARA QUE GESTIONE O BARRAS ENTERAS O ULTIMA BARRA MAS CORTA
                resto = (restoDelTotalPerfiles > BARRALACADOPERFILES || esLacadoEnteras ? BARRALACADOPERFILES : (partimosUltimaBarraPerfiles ? ULTIMABARRALACADOPERFILES : BARRALACADOPERFILES) );  // inicializamos como una barra entera)
                ultimaBarraEscogida = resto + 200; // para el consumo.
                if (ultimaBarraEscogida.equals(BARRALACADOPERFILES + 200)) {
                    contadorBarrasCompletas++;
                } else {
                    contadorBarras3150++;
                }
                for (Barras p : auxListadoPerfilesOrdenado) {
                    //Compruebo si hay cambio de seccion

                    if (resto > (p.getLongitud() + MARGENCORTE)) {
                        resto = resto - (p.getLongitud() + MARGENCORTE);
                        restoDelTotalPerfiles = restoDelTotalPerfiles - (p.getLongitud() + MARGENCORTE);
                        contPerfilesBarra++;
                        if (!p.getPerteneceASeccion().equals(seccionActual)) {
                            seccionesEnBarra += "Seccion " + seccionActual + " [" + contPerfilesSeccionBarra + " perf.] ";
                            seccionActual = p.getPerteneceASeccion();
                            contPerfilesSeccionBarra = 1;
                        } else {
                            contPerfilesSeccionBarra++;
                        }

                    } else {
                        seccionesEnBarra += "Seccion " + seccionActual + " [" + contPerfilesSeccionBarra + " perf.] ";
                        logLacado += "[ ] 1 x " + auxTipoPerfil + " " + ultimaBarraEscogida + " (Total " + contPerfilesBarra + " perfiles en esta barra: " + seccionesEnBarra + ")<br>";
                        //" (Total " + contPerfilesBarra + " perfiles en esta barra: "

                        totalPerfilesConsumidos += ultimaBarraEscogida;
                        seccionesEnBarra = "";
                        //contadorBarrasNuevas++;
                        contPerfilesBarra = 0;
                        contPerfilesSeccionBarra = 0;
                        resto = (restoDelTotalPerfiles > BARRALACADOPERFILES || esLacadoEnteras ? BARRALACADOPERFILES :(partimosUltimaBarraPerfiles ? ULTIMABARRALACADOPERFILES : BARRALACADOPERFILES));
                        ultimaBarraEscogida = resto + 200; // Para el consumo
                        if (ultimaBarraEscogida.equals(BARRALACADOPERFILES + 200)) {
                            contadorBarrasCompletas++;
                        } else {
                            contadorBarras3150++;
                        }
                        resto = resto - (p.getLongitud() + MARGENCORTE);
                        restoDelTotalPerfiles = restoDelTotalPerfiles - (p.getLongitud() + MARGENCORTE);
                        contPerfilesBarra++;
                        if (!p.getPerteneceASeccion().equals(seccionActual)) {
                            //seccionesEnBarra +="Seccion "+ seccionActual + " [" + contPerfilesSeccionBarra + " perf.] ";
                            seccionActual = p.getPerteneceASeccion();
                            contPerfilesSeccionBarra = 1;
                        } else {
                            contPerfilesSeccionBarra++;
                        }

                    }
                }

                // Vemos cuanto hemos consumido de la ultima barra nueva
                // Con esta operacion lo que hacemos es saber cuanto hemos consumido de la ultima barra nueva contada, descontamos una barra y sumamos este resto.
                resto = ultimaBarraEscogida - resto;
                if (resto > 5500 || esLacadoEnteras) {  // Barra nueva
                    seccionesEnBarra += "Seccion " + seccionActual + " [" + contPerfilesSeccionBarra + " perf.] ";
                    logLacado += "[ ] 1 x " + auxTipoPerfil + " " + longitudBarraCompletaSistema + " (Total " + contPerfilesBarra + " perfiles en esta barra: " + seccionesEnBarra + ")<br>";
                    logLacadoPerfiles += (contadorBarrasCompletas > 0 ? "[ ] " + contadorBarrasCompletas + " x " + auxTipoPerfil + " "+longitudBarraCompletaSistema+" <br>" : "");
                    totalPerfilesConsumidos += longitudBarraCompletaSistema;
                    resto = 0.0;
                } else { // Si no , descontamos una barra completa ya que añadiremos el grupo del resto. DIFERENCIAR CUAL ERA LA ULTIMA BARRA ESCOGIDA
                    if (ultimaBarraEscogida.equals(longitudBarraCompletaSistema)) {
                        contadorBarrasCompletas--;

                    } else {  //la ultima escogida era 3150
                        contadorBarras3150--;
                    }
                    if (contadorBarrasCompletas > 0) {
                        logLacadoPerfiles += "[ ] " + contadorBarrasCompletas + " x " + auxTipoPerfil + " "+longitudBarraCompletaSistema+" <br>";
                    }
                    if (contadorBarras3150 > 0) {
                        logLacadoPerfiles += "[ ] " + contadorBarras3150 + " x " + auxTipoPerfil + " " + ultimaBarraEscogida + "<br>";
                    }

                    seccionesEnBarra += "Seccion " + seccionActual + " [" + contPerfilesSeccionBarra + " perf.] ";
                    logLacado += "[ ] 1 x " + auxTipoPerfil + " " + String.format("%1$.1f", (resto + (2 * MARGENLACADO))) + " (Total " + contPerfilesBarra + " perfiles en esta barra: " + seccionesEnBarra + ")<br>";
                    logLacadoPerfiles += "[ ] 1 x " + auxTipoPerfil + " " + String.format("%1$.1f", (resto + (2 * MARGENLACADO))) + " <br>";
                    totalPerfilesConsumidos += Double.valueOf((resto + (2 * MARGENLACADO)));
                }

                System.out.println(logLacadoPerfiles);
            }
            
            if (totalPerfilesConsumidos!=null && totalPerfilesConsumidos>0.0){
                            
                return new StockBarras(auxTipoPerfil, "CRUDO", totalPerfilesConsumidos, auxListado.get(0).getTipoSistema());
            } else {
                return new StockBarras("NO MOCHILIFICABLE", "CRUDO", 0.0, auxListado.get(0).getTipoSistema());
            }
        }
        return null;

    }
    
    private StockBarras calcularLongitudPerfiles(List<Barras> auxListado) {

        Integer contadorBarrasCompletas = 0;
        Integer contadorBarras3150 = 0;
        Integer contPerfilesBarra = 0;
        Integer contPerfilesSeccionBarra = 0;
        Double resto = 0.0; // Inicializamos mas tarde dependiendo de la lonit
        Integer seccionActual = 1; // Aqui la secciones de las barras ya empiezan en 1
        List<Barras> auxListadoPerfiles = new ArrayList<>();
        List<Barras> auxListadoPerfilesSeccion = new ArrayList<>();
        List<Barras> auxListadoPerfilesOrdenado = new ArrayList<>();
        String tipoSistema = "";
        String auxTipoPerfil = null;
        Double ultimaBarraEscogida = 0.0;
        Boolean esLacadoEnteras = lacadoForzado.equals("ENTERAS") || colorAdaptado.equals("CRUSOP");
        totalPerfilesConsumidos = 0.0;
        Boolean partimosUltimaBarraPerfiles = Boolean.FALSE;

        Double restoDelTotalPerfiles = 0.0;
        
        Boolean esCorredera = Boolean.FALSE;

        //Extraemos solo los perfiles de las barras necesidad y aprovechamos para ver cuantas secciones hay
        if (auxListado != null && !auxListado.isEmpty() && !auxListado.get(0).getTipoSistema().contains("INFINIA")) {
            tipoSistema = auxListado.get(0).getTipoSistema()!=null ? auxListado.get(0).getTipoSistema() : "";
            for (Barras b : auxListado) {
                if (b.getTipoPerfil().contains("PERF") || b.getTipoPerfil().contains("PEROD") || b.getTipoPerfil().equals("PER10CRD") || b.getTipoPerfil().equals("PER10MICA")) {
                    auxListadoPerfiles.add(b);
                    auxTipoPerfil = b.getTipoPerfil();
                    restoDelTotalPerfiles += b.getLongitud() + MARGENCORTE; // Acumulamos aqui el total de perfiles para poder controlar las barras de crudo que cogemos, por el tema de las ultimas barras cortas.
                    if (b.getPerteneceASeccion() > seccionActual) {
                        seccionActual = b.getPerteneceASeccion();
                    }
                    if (b.getTipoSistema().startsWith("CORREDERA")){
                        esCorredera = Boolean.TRUE;
                    }
                }
            }
            // Una vez separados, los agrupamos por seccion y los ordenamos por longitud usando el sort de Barras
            if (!auxListadoPerfiles.isEmpty()) {
                for (int i = 1; i <= seccionActual; i++) {
                    for (Barras p : auxListadoPerfiles) {
                        if (p.getPerteneceASeccion().equals(i)) {
                            auxListadoPerfilesSeccion.add(p);
                        }
                    }
                    Collections.sort(auxListadoPerfilesSeccion); // al ordernarlo se quedan de menor a mayor por lo que los invertimos justo despues
                    /*for (int ibs=auxListadoPerfilesSeccion.size()-1; ibs>=0; ibs--){
                        auxListadoPerfilesOrdenado.add(auxListadoPerfilesSeccion.get(ibs));
                    }*/
                    auxListadoPerfilesOrdenado.addAll(auxListadoPerfilesSeccion);
                    auxListadoPerfilesSeccion = new ArrayList();
                }
            }
            // Ya tenemos el array de perfiles ordenados por seccion y longitud.
            if (!auxListadoPerfilesOrdenado.isEmpty()) {
                seccionActual = 1;
                String seccionesEnBarra = "";
                //contadorBarrasNuevas = 1;  // Empezamos con una barra nueva
                //logLacadoPerfiles += "B"+contadorBarrasNuevas+"->";
                logLacado += "PERFILES: Comenzamos cortando barras enteras siguiendo el orden de los perfiles del proyecto. Si hay una barra corta, será para el final de la ultima sección.<br>";
                //INICIALIZAMOS LA BARRAS RESTO SEGUN SEA EL TOTAL DE PERFILES, PARA QUE GESTIONE O BARRAS ENTERAS O ULTIMA BARRA MAS CORTA
                resto = (restoDelTotalPerfiles > BARRALACADOPERFILES || esLacadoEnteras ? BARRALACADOPERFILES : (partimosUltimaBarraPerfiles ? ULTIMABARRALACADOPERFILES : BARRALACADOPERFILES) );  // inicializamos como una barra entera)
                ultimaBarraEscogida = resto + 200; // para el consumo.
                if (ultimaBarraEscogida.equals(BARRALACADOPERFILES + 200)) {
                    contadorBarrasCompletas++;
                } else {
                    contadorBarras3150++;
                }
                for (Barras p : auxListadoPerfilesOrdenado) {
                    //Compruebo si hay cambio de seccion

                    if (resto > (p.getLongitud() + MARGENCORTE)) {
                        resto = resto - (p.getLongitud() + MARGENCORTE);
                        restoDelTotalPerfiles = restoDelTotalPerfiles - (p.getLongitud() + MARGENCORTE);
                        contPerfilesBarra++;
                        if (!p.getPerteneceASeccion().equals(seccionActual)) {
                            seccionesEnBarra += "Seccion " + seccionActual + " [" + contPerfilesSeccionBarra + " perf.] ";
                            seccionActual = p.getPerteneceASeccion();
                            contPerfilesSeccionBarra = 1;
                        } else {
                            contPerfilesSeccionBarra++;
                        }

                    } else {
                        seccionesEnBarra += "Seccion " + seccionActual + " [" + contPerfilesSeccionBarra + " perf.] ";
                        logLacado += "[ ] 1 x " + auxTipoPerfil + " " + ultimaBarraEscogida + " (Total " + contPerfilesBarra + " perfiles en esta barra: " + seccionesEnBarra + ")<br>";
                        //" (Total " + contPerfilesBarra + " perfiles en esta barra: "

                        totalPerfilesConsumidos += ultimaBarraEscogida;
                        seccionesEnBarra = "";
                        //contadorBarrasNuevas++;
                        contPerfilesBarra = 0;
                        contPerfilesSeccionBarra = 0;
                        resto = (restoDelTotalPerfiles > BARRALACADOPERFILES || esLacadoEnteras ? BARRALACADOPERFILES :(partimosUltimaBarraPerfiles ? ULTIMABARRALACADOPERFILES : BARRALACADOPERFILES));
                        ultimaBarraEscogida = resto + 200; // Para el consumo
                        if (ultimaBarraEscogida.equals(BARRALACADOPERFILES + 200)) {
                            contadorBarrasCompletas++;
                        } else {
                            contadorBarras3150++;
                        }
                        resto = resto - (p.getLongitud() + MARGENCORTE);
                        restoDelTotalPerfiles = restoDelTotalPerfiles - (p.getLongitud() + MARGENCORTE);
                        contPerfilesBarra++;
                        if (!p.getPerteneceASeccion().equals(seccionActual)) {
                            //seccionesEnBarra +="Seccion "+ seccionActual + " [" + contPerfilesSeccionBarra + " perf.] ";
                            seccionActual = p.getPerteneceASeccion();
                            contPerfilesSeccionBarra = 1;
                        } else {
                            contPerfilesSeccionBarra++;
                        }

                    }
                }
                // 
                // Vemos cuanto hemos consumido de la ultima barra nueva
                // Con esta operacion lo que hacemos es saber cuanto hemos consumido de la ultima barra nueva contada, descontamos una barra y sumamos este resto.
                resto = ultimaBarraEscogida - resto;
                if (esCorredera && esLacadoEnteras && contadorBarrasCompletas.equals(1)){
                    seccionesEnBarra += "Seccion " + seccionActual + " [" + contPerfilesSeccionBarra + " perf.] ";
                    logLacado += "[ ] 1 x " + auxTipoPerfil + " "+ longitudBarraCompletaSistema +" (Total " + contPerfilesBarra + " perfiles en esta barra: " + seccionesEnBarra + ")<br>";
                    logLacadoPerfiles += (contadorBarrasCompletas > 0 ? "[ ] " + contadorBarrasCompletas + " x " + auxTipoPerfil + " "+longitudBarraCompletaSistema+" <br>" : "");
                    totalPerfilesConsumidos = longitudBarraCompletaSistema;
                    resto = 0.0;
                } else {
                    if (resto > 5500 || esLacadoEnteras) {  // Barra nueva
                        seccionesEnBarra += "Seccion " + seccionActual + " [" + contPerfilesSeccionBarra + " perf.] ";
                        logLacado += "[ ] 1 x " + auxTipoPerfil + " "+longitudBarraCompletaSistema+" (Total " + contPerfilesBarra + " perfiles en esta barra: " + seccionesEnBarra + ")<br>";
                        logLacadoPerfiles += (contadorBarrasCompletas > 0 ? "[ ] " + contadorBarrasCompletas + " x " + auxTipoPerfil + " "+longitudBarraCompletaSistema+" <br>" : "");
                        totalPerfilesConsumidos += longitudBarraCompletaSistema;
                        resto = 0.0;
                    } else { // Si no , descontamos una barra completa ya que añadiremos el grupo del resto. DIFERENCIAR CUAL ERA LA ULTIMA BARRA ESCOGIDA
                        if (ultimaBarraEscogida.equals(longitudBarraCompletaSistema)) {
                            contadorBarrasCompletas--;

                        } else {  //la ultima escogida era 3150
                            contadorBarras3150--;
                        }
                        if (contadorBarrasCompletas > 0) {
                            logLacadoPerfiles += "[ ] " + contadorBarrasCompletas + " x " + auxTipoPerfil + " "+longitudBarraCompletaSistema+" <br>";
                        }
                        if (contadorBarras3150 > 0) {
                            logLacadoPerfiles += "[ ] " + contadorBarras3150 + " x " + auxTipoPerfil + " " + ultimaBarraEscogida + "<br>";
                        }

                        seccionesEnBarra += "Seccion " + seccionActual + " [" + contPerfilesSeccionBarra + " perf.] ";
                        logLacado += "[ ] 1 x " + auxTipoPerfil + " " + String.format("%1$.1f", (resto + (2 * MARGENLACADO))) + " (Total " + contPerfilesBarra + " perfiles en esta barra: " + seccionesEnBarra + ")<br>";
                        logLacadoPerfiles += "[ ] 1 x " + auxTipoPerfil + " " + String.format("%1$.1f", (resto + (2 * MARGENLACADO))) + " <br>";
                        totalPerfilesConsumidos += Double.valueOf((resto + (2 * MARGENLACADO)));
                    }
                }

                System.out.println(logLacadoPerfiles);
            }
            
            if (totalPerfilesConsumidos!=null && totalPerfilesConsumidos>0.0){
                  
                return new StockBarras(auxTipoPerfil, "CRUDO", totalPerfilesConsumidos, auxListado.get(0).getTipoSistema());
            } else {
                return new StockBarras("NO MOCHILIFICABLE", "CRUDO", 0.0, auxListado.get(0).getTipoSistema());
            }
        }
        return null;
    }
    
    

    //Obsoleto
    private List<StockBarras> calcularLongitudOtrasBarras(List<Barras> auxListado) {
        Integer contadorOtrasBarras = 0;
        Double sumaOtrasBarras = 0.0;
        Double perfilMasGrande = 0.0;
        List<StockBarras> auxStockBarras = new ArrayList<>();
        ArrayList<String> auxTiposDePerfil = new ArrayList<>();
        if (auxListado != null && !auxListado.isEmpty()) {
            for (Barras b : auxListado) {
                // Inicializo un array con los tipos de perfiles para no procesar los mochilificables
                if (!auxTiposDePerfil.contains(b.getTipoPerfil()) && otrasBarrasNoMochilificables.contains(b.getTipoPerfil())) {
                    auxTiposDePerfil.add(b.getTipoPerfil());
                }
            }
            if (!auxTiposDePerfil.isEmpty()) {
                for (String s : auxTiposDePerfil) {
                    /*
                    contadorOtrasBarras = 0;
                    sumaOtrasBarras = 0.0;
                    perfilMasGrande = 0.0;
                    for (Barras b : auxListado) {
                        if (b.getTipoPerfil().equals(s)) {
                            contadorOtrasBarras++;
                            sumaOtrasBarras += b.getLongitud() + (2 * MARGENLACADO);
                        }
                    }
                    auxStockBarras.add(new StockBarras(s, "CRUDO", sumaOtrasBarras, auxListado.get(0).getTipoPerfil()));
                    */
                    StockBarras sb = new StockBarras();
                    sb = calcularLongitudTipoPerfil(auxListado, s);
                    if (sb!=null){
                        auxStockBarras.add(sb);
                    }    
                }
            }
        }

        return auxStockBarras;
    }
    
   
    //Obsoleto
    private void estableceMermaYResto(Barras auxBarra, String auxColor, StockBarras auxStockBarra){
        Double auxMerma = 0.0;
        if(auxStockBarra !=null){
            auxMerma = auxStockBarra.getLongitud()-auxBarra.getLongitud();
        } else {
            auxMerma = 0.0;
        }    
        Double grupoResto = Double.valueOf(grupoMenorLongitud(auxMerma));
        if (auxColor.equals("CRUSOP")){
            auxBarra.setMerma(auxMerma);
            auxBarra.setResto(0.0);
        } else {
            auxBarra.setMerma(auxMerma-grupoResto);
            auxBarra.setResto(grupoResto);
        }
    }
    
    private Boolean cumpleCondicionMochilificarCombinadas(Barras auxBarra1, Barras auxBarra2, Boolean mejorada){
        if ((!auxBarra1.getIdentificadorUnicoBarra().equals(auxBarra2.getIdentificadorUnicoBarra()) // NO es la misma barra
                                && auxBarra1.getTipoPerfil().equals(auxBarra2.getTipoPerfil()) // Es el mismo tipo de perfil
                                && auxBarra2.getMochilificada() == 0 && !mejorada)  // no esta ya mochilificada y no esta mejorada
                        && ((auxBarra1.getLongitud() + auxBarra2.getLongitud() + MARGENCORTE + (paraLacar? (2*MARGENLACADO) : 2*DESPUNTE)) < longitudBarraCompletaSistema)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }    
    
    /**
     * Los perfiles se tratan de distinta manera, ya que no contienen el lacado desde appglass, hay que sumarlo si es solo una barra o si queda una barra no completa
     * @param auxBarrasStock
     * @param auxLongBarrasNecesidadLacar
     * @return 
     */
    private String generaCadenaConsumoOld(List<StockBarras> auxBarrasStock, List<Double> auxLongBarrasNecesidad){
        
        String cadena = "", perfil = "";
        List<Integer> yaContados = new ArrayList<>();
        Integer contadorPerfil = 0, i=0, j=0;
        Double longitudTotalPerfil = 0.0;
        Boolean esLacadoEnteras = lacadoForzado.equals("ENTERAS") || colorAdaptado.equals("CRUSOP");
        cadena += "@("+colorAdaptado+"["+ color + "])|";
        if (auxBarrasStock!=null && !auxBarrasStock.isEmpty()){
            if (paraLacar){
                for (i=0; i<auxBarrasStock.size(); i++){
                    longitudTotalPerfil = 0.0;
                    contadorPerfil = 0;
                    if (!yaContados.contains(i)){
                        perfil = auxBarrasStock.get(i).getTipoPerfil();
                        
                        longitudTotalPerfil += (esLacadoEnteras 
                                                         ? ((perfil.startsWith("PERF") || perfil.startsWith("PEROD"))
                                                                ? totalPerfilesConsumidos.intValue():longitudBarraCompletaSistema.intValue())
                                                         :(auxBarrasStock.get(i).getLongitud()-auxLongBarrasNecesidad.get(i)<MINIMOUTILIZABLE? auxBarrasStock.get(i).getLongitud() :auxLongBarrasNecesidad.get(i)));                                 
                        contadorPerfil++;
                        yaContados.add(i);
                        for (j=i+1; j<auxBarrasStock.size(); j++){
                            if (auxBarrasStock.get(j).getTipoPerfil().equals(perfil) && !yaContados.contains(j)){
                                longitudTotalPerfil += ( esLacadoEnteras ? longitudBarraCompletaSistema : (auxBarrasStock.get(j).getLongitud()-auxLongBarrasNecesidad.get(j)<MINIMOUTILIZABLE? auxBarrasStock.get(j).getLongitud() :auxLongBarrasNecesidad.get(j)));
                                yaContados.add(j);
                            }
                        }
                        
                        cadena += perfil + "-" + Math.round(longitudTotalPerfil) + "|";

                    }
                }
                
                return cadena;
            } else {
                for (i=0; i<auxBarrasStock.size(); i++){
                    longitudTotalPerfil = 0.0;
                    contadorPerfil = 0;
                    if (!yaContados.contains(i)){
                        perfil = auxBarrasStock.get(i).getTipoPerfil();
                        longitudTotalPerfil += auxLongBarrasNecesidad.get(i);
                        contadorPerfil++;
                        yaContados.add(i);
                        for (j=i+1; j<auxBarrasStock.size(); j++){
                            if (auxBarrasStock.get(j).getTipoPerfil().equals(perfil) && !yaContados.contains(j)){
                                longitudTotalPerfil += auxLongBarrasNecesidad.get(j);
                                yaContados.add(j);
                            }
                        }
                        cadena += perfil + "-" + Math.round(longitudTotalPerfil) + "|";
                    }
                }
                
                return cadena;
            }
        
        }
        
        return "";
    }
    
    
    // deepsek
    private String generaCadenaConsumo(List<StockBarras> auxBarrasStock, List<Double> auxLongBarrasNecesidad) {
        StringBuilder cadena = new StringBuilder();
        cadena.append("@(").append(colorAdaptado).append("[").append(color).append("])|");

        if (auxBarrasStock == null || auxBarrasStock.isEmpty()) {
            return "";
        }

        boolean esLacadoEnteras = lacadoForzado.equals("ENTERAS") || colorAdaptado.equals("CRUSOP");
        Map<String, Map<Integer, Integer>> perfilLongitudes = new HashMap<>();

        for (int i = 0; i < auxBarrasStock.size(); i++) {
            StockBarras barra = auxBarrasStock.get(i);
            String perfil = barra.getTipoPerfil();
            int longitudConsumida = calcularLongitudConsumida(barra, auxLongBarrasNecesidad.get(i), esLacadoEnteras);

            // Dividir si la longitud consumida supera barras completas
            if (longitudConsumida > longitudBarraCompletaSistema.intValue()) {
                int cantidadBarrasCompletas = longitudConsumida / longitudBarraCompletaSistema.intValue();
                int resto = longitudConsumida % longitudBarraCompletaSistema.intValue();

                if (cantidadBarrasCompletas > 0) {
                    actualizarMapa(perfilLongitudes, perfil, longitudBarraCompletaSistema.intValue(), cantidadBarrasCompletas);
                }
                if (resto > 0) {
                    actualizarMapa(perfilLongitudes, perfil, resto, 1);
                }
            } else {
                actualizarMapa(perfilLongitudes, perfil, longitudConsumida, 1);
            }
        }

        // Construir la cadena de resultado
        for (Map.Entry<String, Map<Integer, Integer>> entradaPerfil : perfilLongitudes.entrySet()) {
            String perfil = entradaPerfil.getKey();
            Map<Integer, Integer> longitudes = entradaPerfil.getValue();

            for (Map.Entry<Integer, Integer> entradaLongitud : longitudes.entrySet()) {
                cadena.append(perfil)
                        .append("-")
                        .append(entradaLongitud.getValue())
                        .append("x")
                        .append(entradaLongitud.getKey())
                        .append("|");
            }
        }

        if (cadena.length() > 0 && cadena.charAt(cadena.length() - 1) == '|') {
            cadena.setLength(cadena.length() - 1);
        }

        return cadena.toString();
    }

    private int calcularLongitudConsumida(StockBarras barra, Double necesidad, boolean esLacadoEnteras) {
        if (esLacadoEnteras) {
            return (barra.getTipoPerfil().startsWith("PERF") || barra.getTipoPerfil().startsWith("PEROD"))
                    ? totalPerfilesConsumidos.intValue()
                    : longitudBarraCompletaSistema.intValue();
        } else {
            double resto = barra.getLongitud() - necesidad;
            return (int) Math.round(resto < MINIMOUTILIZABLE ? barra.getLongitud() : necesidad);
        }
    }

    private void actualizarMapa(Map<String, Map<Integer, Integer>> mapa,
            String perfil,
            int longitud,
            int cantidad) {
        // Crear un HashMap con tipos explícitos si no existe
        mapa.putIfAbsent(perfil, new HashMap<Integer, Integer>());

        // Obtener el mapa de longitudes para el perfil
        Map<Integer, Integer> longitudes = mapa.get(perfil);

        // Actualizar la cantidad para esta longitud
        longitudes.put(longitud, longitudes.getOrDefault(longitud, 0) + cantidad);
    }
    /**
     * Se han dado casos aislados de trabajos que en el corte llegan duplicados, al analizarlos se ha visto que tienen barras de distintas versiones juntas, para asegurarnos vamos a hacer un analisis previo.
     */
    private void analizaSiHayBarrasConDosVersiones () throws Exception{
        Integer ultimaVersion = -1;
        Integer otraVersion = -1;
        Integer versionBarraAnalizada;
        Integer contador = 0;
        if (listaBarrasNecesidad!=null){
            for (Barras b: listaBarrasNecesidad){
                if (!b.getIdentificadorUnicoBarra().contains("REST")){
                    versionBarraAnalizada = Integer.valueOf(b.getIdentificadorUnicoBarra().substring(6, 7));
                    if (contador == 0){
                        ultimaVersion = versionBarraAnalizada;
                        otraVersion = versionBarraAnalizada;
                    } else {
                        if (versionBarraAnalizada.equals(ultimaVersion)){ // No hago nada pero como es el caso mas habitual evito hacer dos preguntas cada vez
                            
                        } else if (versionBarraAnalizada > ultimaVersion){
                            otraVersion = ultimaVersion;
                            ultimaVersion = versionBarraAnalizada;
                        } else if (versionBarraAnalizada < ultimaVersion ) {
                            otraVersion = versionBarraAnalizada;
                        } 
                    }   
                }
            contador++;    
            }
                
        }
        
        if (!otraVersion.equals(ultimaVersion)){
            
            List<Barras> barrasDistintasAUltimaVersion = new ArrayList<>(); 
            // Liberar la primera version si esta mochilificada y eliminar esas barras.
            for (Barras b : listaBarrasNecesidad){
                versionBarraAnalizada = Integer.valueOf(b.getIdentificadorUnicoBarra().substring(6, 7));
                if (!versionBarraAnalizada.equals(ultimaVersion)){
                    barrasDistintasAUltimaVersion.add(b);
                }
            }
            preparaBarras(barrasDistintasAUltimaVersion);
            listaBarrasNecesidad.removeAll(barrasDistintasAUltimaVersion);
            barraServices.borraBarras(barrasDistintasAUltimaVersion);
        }
        
    }

    
    /**
     * Este metodo hace toda la preparacion previa sobre las barras que nos ha pasado appglass. Elimina las barras editadas, separa barras mochilificables
     * (para ordernarlas y agruparlas) de las no mochilificables, asi se gana en rendimiento y logica)
     */
    private void ordenarYAgrupaBarrasNecesidad (){
        
        List<String> tipoPerfiles = new ArrayList<>();
        
        //En un recorrido inicial de todas las barras, separamos
        for (Barras b: listaBarrasNecesidad){
            // SI es un resto o es un perfil no mochilificable
            if (b.getTipoSistema().startsWith("CORTINA")){ // Las barras mochilificables de cortina cruiser son comunes a status y por eso se iguala aqui el tipo sistema
                b.setTipoSistema("CORTINA_STATUS");
            }
            if (!perfilesMochilificables.contains(b.getTipoPerfil()) || b.getIdentificadorUnicoBarra().contains("REST") || b.getLongitud()<=0.0){  // barras longuitud cero o negativas incluso, procedentes de ediciones.
                b.setMochilificada(1);
                auxNecesidadNoMochilificable.add(b);
            } 
            
            //SI es una barra editada, indicaremos que su seccion tiene barras editadas
            if (b.getTagD()!=null && b.getTagD().contains("#E")){
                //b.setMochilificada(1); Vamos a mochilificarla sin problema, ya que tiene los datos correctos.
                auxBarrasEditadas.add(b);
                if (auxNecesidadNoMochilificable.contains(b)){
                    auxNecesidadNoMochilificable.remove(b);
                }
                if(!seccionesEditadas.contains(b.getPerteneceASeccion())){
                    seccionesEditadas.add(b.getPerteneceASeccion());
                }    
            }
            if (otrasBarrasNoMochilificables!=null && otrasBarrasNoMochilificables.contains(b.getTipoPerfil())){
                contieneOtrasBarrasNoMochilificables = Boolean.TRUE;
            }
            if(b.getTipoPerfil().equals("PLT") || b.getTipoPerfil().equals("PU")){
                contienePLToPU = Boolean.TRUE;
            }    
        }
        
        if(auxNecesidadNoMochilificable!=null && !auxNecesidadNoMochilificable.isEmpty()){
            listaBarrasNecesidad.removeAll(auxNecesidadNoMochilificable);
        }    
        
        //Solo quedan las mochilificables
        for (Barras c: listaBarrasNecesidad){
            if(!tipoPerfiles.contains(c.getTipoPerfil())){
                tipoPerfiles.add(c.getTipoPerfil());
            }
        }    

        Collections.sort(listaBarrasNecesidad);
   
        
        for (String perfil : tipoPerfiles){
            for (Barras d: listaBarrasNecesidad){
                if (d.getTipoPerfil().equals(perfil))
                    auxOrdenadoNecesidad.add(d);
            }
        }
        
    }
    
    private void rellenaMochilificaciones(List<Barras> auxListadoBarrasNecesidad) throws Exception {
        //ArrayList<Barras> auxTempBarrasNecesidad = new ArrayList<>();
        ArrayList<Integer> auxIndicesBarrasMochilificadas = new ArrayList<>();
        ArrayList<Mochilificacion> mochilificacionesElegidas = new ArrayList<>();
        Double longAMochilificar = 0.0, longAMochilificar_It2 = 0.0, longAMochilificar_It3 = 0.0, longAMochilificar_It4 = 0.0, longAMochilificar_It5 = 0.0;
        StockBarras sb = new StockBarras(), sb2 = new StockBarras(), sb3 = new StockBarras(), sb4 = new StockBarras(), sb5 = new StockBarras();
        Integer ultimaMochilificacion = 0;
        String logMochilificaciones = "";
        Double longitudTotalMochilificada= 0.0;
        Integer numeroDeBarrasMochilificadas = 0;
        Double mermaTotal = 0.0;
        Integer contGrupoStockMochilificacion = 0;
        
        Double ultima_j=0.0, ultima_k=0.0, ultima_l=0.0, ultima_m=0.0;  // Se usa para no repetir combinaciones ya calculadas, por ejemplo en una curva muy grande con barras iguales (ej 44659)
        Boolean mochilificacionYaObiligada = Boolean.FALSE;
        
        // INVERTIMOS PARA PODER MOCHILIFICAR LAS BARAS PEQUEÑAS EN OBLIGADAS POR STOCK
        Collections.reverse(auxListadoBarrasNecesidad);
        // UNA ITERACION
        for (int i = 0; i < auxListadoBarrasNecesidad.size(); i++) {
            ultima_j=0.0;
            ultima_k=0.0; 
            ultima_l=0.0;
            ultima_m=0.0;
            mochilificacionYaObiligada = Boolean.FALSE;
            if (auxListadoBarrasNecesidad.get(i).getMochilificada().equals(0)) {
                auxIndicesBarrasMochilificadas = new ArrayList<>();
                auxIndicesBarrasMochilificadas.add(i);
                longAMochilificar = auxListadoBarrasNecesidad.get(i).getLongitud() + (paraLacar ? (2 * MARGENLACADO) : 2 * DESPUNTE);
                sb = buscarBarraCompatibleEnStock(longAMochilificar, auxListadoBarrasNecesidad.get(i).getTipoPerfil(), listaBarrasStock);
                mochilificaciones.add(new Mochilificacion(auxIndicesBarrasMochilificadas, longAMochilificar, sb));
                ultimaMochilificacion = mochilificaciones.size() - 1;
                mermaYRestoMochilificacion(mochilificaciones.get(ultimaMochilificacion),listaBarrasStock);  
                logMochilificaciones += mochilificaciones.get(ultimaMochilificacion).toString();
                if (barraObligadaPorStock){
                    mochilificacionYaObiligada = Boolean.TRUE; // si la mochilificacion da como resultado una barra de la que tenemos mucho stock, no compruebo mejores mochilificaciones, esto sirve para controlar el estocaje de barras.
                }
                for (int j = i + 1; (j < auxListadoBarrasNecesidad.size() && !mochilificacionYaObiligada) ; j++) { 
                    longAMochilificar_It2 = longAMochilificar + auxListadoBarrasNecesidad.get(j).getLongitud() + MARGENCORTE;  // Ya esta incluido el despunte o el margen del lacado en la barra sola, al mochilificar 2 solo metemos margen de corte
                    if ((auxListadoBarrasNecesidad.get(j).getMochilificada() != null && auxListadoBarrasNecesidad.get(j).getMochilificada().equals(0))
                            && (auxListadoBarrasNecesidad.get(j).getTipoPerfil().equals(auxListadoBarrasNecesidad.get(i).getTipoPerfil()))
                            && (longAMochilificar_It2 <= (paraLacar ? longitudBarraCompletaSistema.intValue()-200 : longitudBarraCompletaSistema.intValue()))
                            && !ultima_j.equals(auxListadoBarrasNecesidad.get(j).getLongitud())) {  // Si J es mochilificable sigo
                        auxIndicesBarrasMochilificadas = new ArrayList<>();
                        auxIndicesBarrasMochilificadas.add(i);
                        auxIndicesBarrasMochilificadas.add(j);
                        sb2 = buscarBarraCompatibleEnStock(longAMochilificar_It2, auxListadoBarrasNecesidad.get(i).getTipoPerfil(), listaBarrasStock);
                        mochilificaciones.add(new Mochilificacion(auxIndicesBarrasMochilificadas, longAMochilificar_It2, sb2));

                        ultimaMochilificacion = mochilificaciones.size() - 1;
                        mermaYRestoMochilificacion(mochilificaciones.get(ultimaMochilificacion),listaBarrasStock);
                        logMochilificaciones += mochilificaciones.get(ultimaMochilificacion).toString();
                        ultima_j = auxListadoBarrasNecesidad.get(j).getLongitud();
                        if (barraObligadaPorStock){
                            mochilificacionYaObiligada = Boolean.TRUE; // si la mochilificacion da como resultado una barra de la que tenemos mucho stock, no compruebo mejores mochilificaciones, esto sirve para controlar el estocaje de barras.
                        }
                        for (int k = j + 1; (k < auxListadoBarrasNecesidad.size() && !mochilificacionYaObiligada); k++) {
                            longAMochilificar_It3 = longAMochilificar_It2 + auxListadoBarrasNecesidad.get(k).getLongitud() + MARGENCORTE;
                            if ((auxListadoBarrasNecesidad.get(k).getMochilificada() != null && auxListadoBarrasNecesidad.get(k).getMochilificada().equals(0))
                                    && (auxListadoBarrasNecesidad.get(k).getTipoPerfil().equals(auxListadoBarrasNecesidad.get(i).getTipoPerfil()))
                                    && (longAMochilificar_It3 <= (paraLacar ? longitudBarraCompletaSistema.intValue()-200 : longitudBarraCompletaSistema.intValue()))
                                    && !ultima_k.equals(auxListadoBarrasNecesidad.get(k).getLongitud())) {  // Si k es mochilificable sigo
                                auxIndicesBarrasMochilificadas = new ArrayList<>();
                                auxIndicesBarrasMochilificadas.add(i);
                                auxIndicesBarrasMochilificadas.add(j);
                                auxIndicesBarrasMochilificadas.add(k);
                                sb3 = buscarBarraCompatibleEnStock(longAMochilificar_It3, auxListadoBarrasNecesidad.get(i).getTipoPerfil(), listaBarrasStock);
                                mochilificaciones.add(new Mochilificacion(auxIndicesBarrasMochilificadas, longAMochilificar_It3, sb3));
                                ultimaMochilificacion = mochilificaciones.size() - 1;
                                mermaYRestoMochilificacion(mochilificaciones.get(ultimaMochilificacion),listaBarrasStock);
                                logMochilificaciones += mochilificaciones.get(ultimaMochilificacion).toString();
                                ultima_k = auxListadoBarrasNecesidad.get(k).getLongitud();
                                if (barraObligadaPorStock){
                                    mochilificacionYaObiligada = Boolean.TRUE; // si la mochilificacion da como resultado una barra de la que tenemos mucho stock, no compruebo mejores mochilificaciones, esto sirve para controlar el estocaje de barras.
                                }
                                for (int l = k + 1; (l < auxListadoBarrasNecesidad.size() && !mochilificacionYaObiligada); l++) {
                                    longAMochilificar_It4 = longAMochilificar_It3 + auxListadoBarrasNecesidad.get(l).getLongitud() + MARGENCORTE;
                                    if ((auxListadoBarrasNecesidad.get(l).getMochilificada() != null && auxListadoBarrasNecesidad.get(l).getMochilificada().equals(0))
                                            && (auxListadoBarrasNecesidad.get(l).getTipoPerfil().equals(auxListadoBarrasNecesidad.get(i).getTipoPerfil()))
                                            && (longAMochilificar_It4 <= (paraLacar ? longitudBarraCompletaSistema.intValue()-200 : longitudBarraCompletaSistema.intValue()))
                                            && !ultima_l.equals(auxListadoBarrasNecesidad.get(l).getLongitud())) { // Si l es mochilificable sigo
                                        auxIndicesBarrasMochilificadas = new ArrayList<>();
                                        auxIndicesBarrasMochilificadas.add(i);
                                        auxIndicesBarrasMochilificadas.add(j);
                                        auxIndicesBarrasMochilificadas.add(k);
                                        auxIndicesBarrasMochilificadas.add(l);
                                        sb4 = buscarBarraCompatibleEnStock(longAMochilificar_It4, auxListadoBarrasNecesidad.get(i).getTipoPerfil(), listaBarrasStock);
                                        mochilificaciones.add(new Mochilificacion(auxIndicesBarrasMochilificadas, longAMochilificar_It4, sb4));
                                        ultimaMochilificacion = mochilificaciones.size() - 1;
                                        mermaYRestoMochilificacion(mochilificaciones.get(ultimaMochilificacion),listaBarrasStock);
                                        logMochilificaciones += mochilificaciones.get(ultimaMochilificacion).toString();
                                        ultima_l= auxListadoBarrasNecesidad.get(l).getLongitud();
                                        if (barraObligadaPorStock){
                                            mochilificacionYaObiligada = Boolean.TRUE; // si la mochilificacion da como resultado una barra de la que tenemos mucho stock, no compruebo mejores mochilificaciones, esto sirve para controlar el estocaje de barras.
                                        }
                                        for (int m = l + 1; (m < auxListadoBarrasNecesidad.size() && !mochilificacionYaObiligada); m++) {
                                            longAMochilificar_It5 = longAMochilificar_It4 + auxListadoBarrasNecesidad.get(m).getLongitud() + MARGENCORTE;
                                            if ((auxListadoBarrasNecesidad.get(m).getMochilificada() != null && auxListadoBarrasNecesidad.get(m).getMochilificada().equals(0))
                                                    && (auxListadoBarrasNecesidad.get(m).getTipoPerfil().equals(auxListadoBarrasNecesidad.get(i).getTipoPerfil()))
                                                    && (longAMochilificar_It5 <= (paraLacar ? longitudBarraCompletaSistema.intValue()-200 : longitudBarraCompletaSistema.intValue()))
                                                    && !ultima_m.equals(auxListadoBarrasNecesidad.get(m).getLongitud())) { // Si l es mochilificable sigo
                                                auxIndicesBarrasMochilificadas = new ArrayList<>();
                                                auxIndicesBarrasMochilificadas.add(i);
                                                auxIndicesBarrasMochilificadas.add(j);
                                                auxIndicesBarrasMochilificadas.add(k);
                                                auxIndicesBarrasMochilificadas.add(l);
                                                auxIndicesBarrasMochilificadas.add(m);
                                                sb5 = buscarBarraCompatibleEnStock(longAMochilificar_It5, auxListadoBarrasNecesidad.get(i).getTipoPerfil(), listaBarrasStock);
                                                mochilificaciones.add(new Mochilificacion(auxIndicesBarrasMochilificadas, longAMochilificar_It5, sb5));
                                                ultimaMochilificacion = mochilificaciones.size() - 1;
                                                mermaYRestoMochilificacion(mochilificaciones.get(ultimaMochilificacion),listaBarrasStock);
                                                logMochilificaciones += mochilificaciones.get(ultimaMochilificacion).toString();
                                                ultima_m= auxListadoBarrasNecesidad.get(m).getLongitud();
                                                if (barraObligadaPorStock){
                                                    mochilificacionYaObiligada = Boolean.TRUE; // si la mochilificacion da como resultado una barra de la que tenemos mucho stock, no compruebo mejores mochilificaciones, esto sirve para controlar el estocaje de barras.
                                                }
                                            }  // m es mochilificable
                                        } // For de m
                                    }  // l es mochilificable
                                } //For de l
                            } // k es mochilificable
                        } //for de k
                    } //j es mochilificable
                } //for de j
                //TODO elegir mejor mochilificacion y poner las barras involucradas como mochilificadas, establecer a la primera la merma y el resto, etc....
                if (mochilificaciones!=null && !mochilificaciones.isEmpty()){
                    contGrupoStockMochilificacion++;
                    mochilificacionesElegidas.add(mejorMochilificacion(mochilificaciones, auxListadoBarrasNecesidad, contGrupoStockMochilificacion));
                    mochilificaciones = new ArrayList<>();
                }
            } //i es mochilificable
        } //for de i
        
        if (!logMochilificaciones.isEmpty()) {
            
            ArrayList<Mochilificacion> mochilificacionesAgrupadas = new ArrayList<>();
            ArrayList<Mochilificacion> mochilificacionesParaBorrar = new ArrayList<>();
            Collections.sort(mochilificacionesElegidas);
            for (Mochilificacion m : mochilificacionesElegidas) {
                Mochilificacion mCompatible = mochilificacionCompatible(m.getStockBarra().getLongitud(), m.getPerfil(), mochilificacionesElegidas, mochilificacionesParaBorrar);
                if (mCompatible !=null){
                    System.out.println("AGRUPAR ESTAS MOCHIS");
                    mochilificacionesAgrupadas.add(new Mochilificacion(m,mCompatible));
                    mochilificacionesParaBorrar.add(m);
                    mochilificacionesParaBorrar.add(mCompatible);
                } 
            }    
            mochilificacionesElegidas.removeAll(mochilificacionesParaBorrar);
            mochilificacionesElegidas.addAll(mochilificacionesAgrupadas);
            
            logMochilificaciones += "\n";
            String logMochilificacionesElegidas = "";
            
            for (Mochilificacion m : mochilificacionesElegidas) {
                //contGrupoStockMochilificacion++;
                //asignaResultadosABarras(m, auxListadoBarrasNecesidad, contGrupoStockMochilificacion);
                logMochilificacionesElegidas += m.toString();
                longitudTotalMochilificada += m.getLongitud();
                mermaTotal += m.getMerma();
                numeroDeBarrasMochilificadas += m.getIndiceBarrasMochilificadas().size();
                LogBarra logBarra = new LogBarra(new Timestamp (Calendar.getInstance().getTimeInMillis()), referenciaProyecto, m);
                logBarraServices.addLogBarra(logBarra);
            }
            logResultadoTotal = "Total longitud mochilificada: " + String.format("%1$.1f",longitudTotalMochilificada) + " Total Merma: " + String.format("%1$.1f",mermaTotal) + " Numero de Barras: " + numeroDeBarrasMochilificadas 
                    + " Merma media por corte: " + String.format("%1$.1f",mermaTotal/numeroDeBarrasMochilificadas) + " Porcentaje: " + String.format("%1$.1f",(mermaTotal*100/longitudTotalMochilificada));
            
            logMochilificaciones = logMochilificacionesElegidas + "\n" + logMochilificaciones;
            
            //new FileUtil().logMochilificacionesAtxt(referenciaProyecto, logMochilificaciones);
        }
       
    }
    
    private Mochilificacion mochilificacionCompatible (Double longitudRequerida, String tipoPerfil, ArrayList<Mochilificacion> mochilificaciones, ArrayList<Mochilificacion> yaAgrupadas){
        for (Mochilificacion mc : mochilificaciones){
            if (!yaAgrupadas.contains(mc) &&  mc.getPerfil().equals(tipoPerfil) && mc.getResto().equals(longitudRequerida)){
                return mc;
            }
        }
        return null;
    }
    
    private void mermaYRestoMochilificacion(Mochilificacion mochilificacion, List<StockBarras> auxStockBarras){
        Integer mermaPorConstantes = (paraLacar ? (2 * MARGENLACADO) : 2 * DESPUNTE) + ((mochilificacion.getIndiceBarrasMochilificadas().size()-1) * MARGENCORTE);
        Boolean produceResto = (mochilificacion.getMerma() + mermaPorConstantes) >= MINIMOUTILIZABLE && !colorAdaptado.equals("CRUSOP");
        Boolean castigadaPorRestoEstocado = Boolean.FALSE;
        
        if (mochilificacion.getStockBarra().getLongitud().equals(longitudBarraCompletaSistema)) {
            Double grupoStockResto = Double.valueOf(grupoMenorLongitud(mochilificacion.getMerma() + mermaPorConstantes));
            mochilificacion.setMerma(produceResto
                    ? (mochilificacion.getMerma() + mermaPorConstantes) - grupoStockResto
                    : mochilificacion.getMerma() + mermaPorConstantes);
            // EN EL CASO DE QUE SE PRODUCZA UN RESTO VAMOS A INTENTAR PENALIZAR ESTA MOCHILIFICACION PARA QUE NO SEA ELEGIDA SI ESA BARRA DE RESTO TIENE YA MUCHO STOCK.
            if (produceResto){
                for (StockBarras sb : auxStockBarras){
                    if (sb.getTipoPerfil().equals(mochilificacion.getStockBarra().getTipoPerfil()) && sb.getLongitud().equals(grupoStockResto)){
                          if (sb.getUnidades()>sb.getStockForzar()){
                            //mochilificacion.setMerma(mochilificacion.getMerma()+500);
                            castigadaPorRestoEstocado = Boolean.TRUE;
                            
                        }
                        break;
                    }
                }
                
            }
            
            mochilificacion.setResto(!colorAdaptado.equals("CRUSOP")? Double.valueOf(grupoMenorLongitud(longitudBarraCompletaSistema - mochilificacion.getLongitud())):0.0); // Si es lacadoEnteras no hay resto
        } else {
            
            mochilificacion.setMerma(mochilificacion.getStockBarra().getLongitud() - mochilificacion.getLongitud() + mermaPorConstantes);  // TODO AÑADIR LAS CTES DE CORTE Y DESPUNTE
            mochilificacion.setResto(0.0);
        }
        if (castigadaPorRestoEstocado){
            mochilificacion.setMermaMedia((mochilificacion.getMerma()/mochilificacion.getIndiceBarrasMochilificadas().size()) + 500);
            mochilificacion.setObservaciones("PERJUDICADA POR RESTO ESTOCADO");
        } else {
            mochilificacion.setMermaMedia(mochilificacion.getMerma()/mochilificacion.getIndiceBarrasMochilificadas().size());
        }
        //mochilificacion.setMermaMedia(mochilificacion.getMerma()/((mochilificacion.getLongitud()-mermaPorConstantes)*100));
        if (barraObligadaPorStock){
            mochilificacion.setObligadaStock(barraObligadaPorStock);
        }
    }
    
    private Mochilificacion mejorMochilificacion (ArrayList<Mochilificacion> auxMochilificaciones, List<Barras> auxListadoBarras,Integer contadorParaIdentificadorGrupoStock){
        Mochilificacion mejorMochilificacion = null;
        Double mermaMediaMinima=longitudBarraCompletaSistema;
        Integer indiceMejorMochilificacion = 0;

        ArrayList<Mochilificacion> mochilificacionesObligadasPorStock = new ArrayList<>();
                
        if (auxMochilificaciones!=null && !auxMochilificaciones.isEmpty()){
            
            //inicializamos los valores con la primera mochilificacion
            mejorMochilificacion = auxMochilificaciones.get(0);
            mermaMediaMinima = auxMochilificaciones.get(0).getMermaMedia();
            indiceMejorMochilificacion = 0;
            
            for (Mochilificacion ms : auxMochilificaciones){
                if (ms.getObligadaStock()){
                    mochilificacionesObligadasPorStock.add(ms);
                }
            }
            if (mochilificacionesObligadasPorStock.isEmpty()){
                for (Mochilificacion m : auxMochilificaciones){
                   if (m.getMermaMedia() < mermaMediaMinima){
                        mejorMochilificacion = m;
                        mermaMediaMinima = m.getMermaMedia();
                        indiceMejorMochilificacion = auxMochilificaciones.indexOf(m);
                    }
                }
            } else {
                for (Mochilificacion m : mochilificacionesObligadasPorStock){
                    if (m.getMermaMedia() < mermaMediaMinima){
                        mejorMochilificacion = m;
                        mermaMediaMinima = m.getMermaMedia();
                        indiceMejorMochilificacion = auxMochilificaciones.indexOf(m);
                    }
                }
            }
            
            // TODO FUNCION ASIGNAR DATOS A BARRAS DE UNA MOCHILIFICACION ELEGIDA

            //Aqui tengo ya la mejor mochilificacion posible puedo poner las barras con sus valores.
            asignaResultadosABarras(mejorMochilificacion, auxListadoBarras, contadorParaIdentificadorGrupoStock);
        }
            return mejorMochilificacion;
    }
    

    private void asignaResultadosABarras(Mochilificacion mochilificacion, List<Barras> auxListadoBarras, Integer contadorParaIdentificadorGrupoStock){
        ArrayList<Integer> indiceBarrasMoch = new ArrayList<>();
        Integer contBarra = 0;
        String cadenaBarraCorte ="";
        
            indiceBarrasMoch.addAll(mochilificacion.getIndiceBarrasMochilificadas());
            contBarra = 1;
            Boolean esBarraCompleta = mochilificacion.getStockBarra().getLongitud().equals(longitudBarraCompletaSistema);
            Boolean generaResto = (mochilificacion.getResto()!=null && mochilificacion.getResto()>MINIMOUTILIZABLE) && !lacadoForzado.equals("ENTERAS");
            for (Integer i : indiceBarrasMoch){
                if (contBarra.equals(1)){  // a la primera barra de un conjunto de barras se le coloca la merma general y el resto asociado, las desmas llevaran 0
                    auxListadoBarras.get(i).setMerma(mochilificacion.getMerma());
                    auxListadoBarras.get(i).setResto(mochilificacion.getResto());
                    
                } else {
                    auxListadoBarras.get(i).setMerma(0.0);
                    auxListadoBarras.get(i).setResto(0.0);
                    
                }
                auxListadoBarras.get(i).setMochilificada(contBarra);
                contBarra++;
                if (paraLacar && esBarraCompleta){ // Aqui indicaremos con -1 aquellas barras que se han cogido barra completa de crudo pero se envian a lacar con una medida inferior, generando un resto.
                    auxListadoBarras.get(i).setGrupoStock(-1);
                    //cadenaBarraCorte ="@" + String.format("%1$.1f",(auxListadoBarras.get(i).getLongitud()+ (2*MARGENLACADO))) + "(" + contadorParaIdentificadorGrupoStock +")";
                    cadenaBarraCorte ="@" + (generaResto ? String.format("%1$.1f",(mochilificacion.getLongitud())):"("+longitudBarraCompletaSistema+")") + "(" + contadorParaIdentificadorGrupoStock +")";
                    
                    auxListadoBarras.get(i).setTagD((auxListadoBarras.get(i).getTagD()!=null 
                                                                        ? (!auxListadoBarras.get(i).getTagD().contains("@")? auxListadoBarras.get(i).getTagD() + cadenaBarraCorte  : auxListadoBarras.get(i).getTagD().substring(0,auxListadoBarras.get(i).getTagD().indexOf("@")) + cadenaBarraCorte)
                                                                        : cadenaBarraCorte));
                    
                    
                } else {
                    auxListadoBarras.get(i).setGrupoStock((int)mochilificacion.getStockBarra().getId());
                }
                auxListadoBarras.get(i).setIdentificador_stock(contadorParaIdentificadorGrupoStock);
                
            }
            
            if (paraLacar){
                if (esBarraCompleta){
                    auxBarrasStockLacar.add(crearBarraCompleta(auxListadoBarras.get(mochilificacion.getIndiceBarrasMochilificadas().get(0))));
                    auxBarrasStockLacarNecesidad.add(mochilificacion.getLongitud());
                    if (mochilificacion.getResto()>0.0){  // Meter en mejor mochilificacion que si es crusop el resto sea cero, asi controlamos todo en un solo sitio, tambien comprobar que el resto sea el grupo adecuado
                        logLacado += "[ ] 1 x " + mochilificacion.getStockBarra().getTipoPerfil() + ", " + String.format("%1$.1f",mochilificacion.getLongitud()) + " (" ;
                    } else {
                        logLacado += "[ ] 1 x " + mochilificacion.getStockBarra().getTipoPerfil() + ", " + " ("+longitudBarraCompletaSistema+")" + " (" ;
                    }
                    
                } else {
                    auxBarrasStockLacar.add(mochilificacion.getStockBarra());
                    auxBarrasStockLacarNecesidad.add(mochilificacion.getLongitud());
                    logLacado += "[ ] 1 x " + mochilificacion.getStockBarra().getTipoPerfil() + ", " + (esGrupo(mochilificacion.getStockBarra().getLongitud())? "("+mochilificacion.getStockBarra().getLongitud()+")":mochilificacion.getLongitud()) + "(" ;
                }    
                int indiceBarra = 0;

                for (int j=0; j<mochilificacion.getIndiceBarrasMochilificadas().size();j++){
                        indiceBarra = mochilificacion.getIndiceBarrasMochilificadas().get(j);
                        logLacado+= "S:" + auxListadoBarras.get(indiceBarra).getPerteneceASeccion() + " " + auxListadoBarras.get(indiceBarra).getTipoPerfil() + " " + auxListadoBarras.get(indiceBarra).getPosicion() + " " + String.format("%1$.1f",auxListadoBarras.get(indiceBarra).getLongitud())+"|";
                }
                logLacado +=  ")<br> ";    
                
            } else {
                if (esBarraCompleta){
                    auxBarrasStockColorStock.add(crearBarraCompleta(auxListadoBarras.get(mochilificacion.getIndiceBarrasMochilificadas().get(0))));
                    auxBarrasStockNecesidad.add(mochilificacion.getLongitud());
                } else {
                    auxBarrasStockColorStock.add(mochilificacion.getStockBarra());
                    auxBarrasStockNecesidad.add(mochilificacion.getLongitud());
                    
                }    
            }
            
            //Rellenamos el log pasando la mochilificacion y la referencia del proyecto
            rellenaLogMochilificacion(mochilificacion, referenciaProyecto);
            
            //Agregamos resto si lo hay
            if (mochilificacion.getResto()>0.0){
                auxRestosBarraNueva.add(crearBarraResto(auxListadoBarras.get(mochilificacion.getIndiceBarrasMochilificadas().get(0)),contadorParaIdentificadorGrupoStock,mochilificacion.getLongitud()));
            }
            //Descontamos barra de stock si no es barra completa
            if (!esBarraCompleta){
                listaBarrasStock.get(listaBarrasStock.indexOf(mochilificacion.getStockBarra())).setUnidades(listaBarrasStock.get(listaBarrasStock.indexOf(mochilificacion.getStockBarra())).getUnidades()-1);
            }    
            
        }
        
        
    
    
    private void rellenaLogMochilificacion (Mochilificacion auxMejorMochilificacion, String auxReferencia){
        //Rellenamos log de mochilificacion
            logger = new AuxLogger(auxReferencia, auxMejorMochilificacion.getStockBarra().getLongitud(), colorAdaptado, 
                    auxMejorMochilificacion.getLongitud(),auxMejorMochilificacion.getIndiceBarrasMochilificadas().size(),(auxMejorMochilificacion.getMerma()), auxMejorMochilificacion.getPerfil());
            logger.setResto(auxMejorMochilificacion.getResto());
            if (auxMejorMochilificacion.getObligadaStock()){
                    logger.setObservaciones("Obligada Stock");
            }
            listaLog.add(logger);
        
    }
   
    public Boolean generaFichero (){
        return ((emisor == 2) || (paraLacar && emisor == 0) || (!paraLacar && emisor == 1));
    }
    
}