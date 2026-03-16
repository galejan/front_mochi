/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilidades;
import com.todocristal.fabrica.webservice.model.Barras;
import com.todocristal.fabrica.webservice.services.BarraServices;
import com.todocristal.fabrica.webservice.model.StockBarras;
import com.todocristal.fabrica.webservice.services.StockBarrasServices;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMImplementation;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


/**
 *
 * @author rafael
 */
public class GeneradorXML {
    private final static String OUTPUT_FOLDER_TRONZADORA ="/home/ftp/tronzadora/";
    
    // Etiquetas de XML para m�quina de cortes
    private final static String JOB = "JOB";    
    private final static String VERSION = "VER"; 
    private final static String VERSION_X = "MJ";     
    private final static String VERSION_Y = "MN";
    private final static String CABECERA = "HEAD";    
    private final static String CABECERA_GRUPO = "PDAT";
    private final static String CABECERA_GRUPO_CODIGO = "CODE";
    private final static String CABECERA_GRUPO_COLORINT = "DICL";
    private final static String CABECERA_GRUPO_COLOREXT = "DOCL";
    private final static String CABECERA_GRUPO_CANTIDAD = "BQTY";
    private final static String BARRA = "BODY";
    private final static String BARRA_DATOS = "BAR";
    private final static String BARRA_DATOS_TIPO = "BRAN";  //Es un valor fijo 'TODOCRISTAL'
    private final static String BARRA_DATOS_SISTEMA = "SYST";   //Depende del tipo de sistema 'CORTINA', 'CORREDERA' etc...
    private final static String BARRA_DATOS_CODIGO = "CODE";    //Identifica la familia del perfil 'PERFIL06', 'CARRIL', etc..
    private final static String BARRA_DATOS_COLORINT = "DICL";  //Lo utilizamos como indicador para facilitar la medida del perfil.
    private final static String BARRA_DATOS_COLOREXT = "DOCL";
    private final static String BARRA_DATOS_LONG= "LEN";
    private final static String BARRA_DATOS_RETAL = "LENR";
    private final static String BARRA_DATOS_GROSOR = "H";
    private final static String BARRA_DATOS_CANTIDAD = "MLT"; //Siempre debe ser 1 por dise�o tronzadora
    private final static String BARRA_DATOS_CORTE = "CUT";
    private final static String BARRA_DATOS_CORTE_ANGIZQUIERDO = "ANGL";
    private final static String BARRA_DATOS_CORTE_ANGDERECHO = "ANGR";
    private final static String BARRA_DATOS_CORTE_LONGINFERIOR = "IL";
    private final static String BARRA_DATOS_CORTE_LONGSUPERIOR = "OL";
    private final static String BARRA_DATOS_CORTE_CODIGOUNICO = "BCOD";  //No debe haber dos c�digos iguales.
    private final static String BARRA_DATOS_CORTE_ETIQUETA = "LBL";
    private List<String> coloresStock = null;
    private String lacadoForzado = "NO";
    private Boolean paraLacar=Boolean.FALSE;
    
    private BarraServices barraServices;
    private List<Barras> listadoBarrasSistema = new ArrayList<>();
    private List<Barras> auxListadoBorrarRestos = new ArrayList();
    private List<Barras> auxBarrasEditadas = new ArrayList();
    
    private StockBarrasServices stockBarraServices;
    private List<StockBarras> listadoBarrasStock = new ArrayList<>();
    
    
    
    public GeneradorXML(BarraServices barraServices, StockBarrasServices stockBarraServices) throws Exception {
        this.barraServices = barraServices;
        this.stockBarraServices = stockBarraServices;
    }
    
    public String generarXMLCortes(String referencia, Document document, Integer version, List<Barras> auxlistadoBarrasSistema, Boolean auxVariosColores, Integer casosMaquinaNueva) throws SAXException, Exception{
        
        String datosBarraStock= "";
        String datosBarraStockLen= "";
        String seccionYColor = "";
        Integer indiceArroba = 0;
        paraLacar = Boolean.FALSE;
        Boolean maquinaNueva = casosMaquinaNueva.equals(0);
        Boolean maquinaNuevaSinPerfiles = casosMaquinaNueva.equals(1);
        
        List<Barras> auxListadoNOPerfiles = new ArrayList<>();
        List<Barras> auxListadoPerfiles = new ArrayList<>();
        List<Barras> auxListadoPerfilesSeccion = new ArrayList<>();
        List<Barras> auxListadoPerfilesOrdenado = new ArrayList<>();
                
        //this.listadoBarrasSistema = barraServices.getBarrasByProyecto(idProyecto);   
        
        // Division por colores
        //calculaBarrasSistema(listadoBarrasSistema, proyecto);
        
        
        
        if (auxlistadoBarrasSistema != null && !auxlistadoBarrasSistema.isEmpty()) {
            
            lacadoForzado = barraServices.comprobarSiHayLacadoForzado(auxlistadoBarrasSistema);
            
            for(Barras b : auxlistadoBarrasSistema){   
                if (b.getTagD()!=null && b.getTagD().contains("#E")){
                    auxBarrasEditadas.add(b);
                }
            }
            
            if (auxBarrasEditadas!=null && !auxBarrasEditadas.isEmpty()){
                auxlistadoBarrasSistema.removeAll(auxBarrasEditadas);
            }
            
            if (!auxlistadoBarrasSistema.isEmpty()) {
                auxlistadoBarrasSistema = agruparBarrasStock(auxlistadoBarrasSistema);
                String tipoPerfil = barraServices.grosorPerfil(auxlistadoBarrasSistema);
                coloresStock = barraServices.inicializarColores(auxlistadoBarrasSistema.get(0).getTipoSistema(), (tipoPerfil!=null && tipoPerfil.equals("PERF10")));
                paraLacar = barraServices.paraLacar(barraServices.colorAluminio(auxlistadoBarrasSistema),barraServices.grosorPerfil(auxlistadoBarrasSistema),lacadoForzado, coloresStock);
                // Cargamos las barras compatibles

                this.listadoBarrasStock = stockBarraServices.getStockBarras(barraServices.adaptaColorALacado(barraServices.colorAluminio(auxlistadoBarrasSistema), lacadoForzado, paraLacar), auxlistadoBarrasSistema.get(0).getTipoSistema());



                if (paraLacar) {
                    for (Barras b: auxlistadoBarrasSistema){
                        if (b.getIdentificadorUnicoBarra().startsWith("REST")){
                            auxListadoBorrarRestos.add(b);
                        }
                    }
                    for (Barras r: auxListadoBorrarRestos){
                        auxlistadoBarrasSistema.remove(r);
                    }        
                }    

                //Comienzo de generaci�n de XML CORTES
                
                // TODO Separar barras carril y compensador de perfiles
                String barraCompletaSistema = auxlistadoBarrasSistema.get(0).getTipoSistema().contains("INFINIA") ? "7000.0" : "6300.0";
                Integer seccionActual = 1;
                for (Barras b : auxlistadoBarrasSistema) {
                    if (b.getTipoPerfil().contains("PERF") || b.getTipoPerfil().contains("PEROD")) {
                        auxListadoPerfiles.add(b);
                        if (b.getPerteneceASeccion() > seccionActual) {
                            seccionActual = b.getPerteneceASeccion();
                        }
                    } else {
                        auxListadoNOPerfiles.add(b);
                    }
                }
                // Una vez separados, los agrupamos por seccion y los ordenamos por longitud usando el sort de Barras
                if (auxListadoPerfiles != null) {
                    
                    for (int i = 1; i <= seccionActual; i++) {
                        for (Barras p : auxListadoPerfiles) {
                            if (p.getPerteneceASeccion().equals(i)) {
                                auxListadoPerfilesSeccion.add(p);
                            }
                        }
                        Collections.sort(auxListadoPerfilesSeccion);
                        auxListadoPerfilesOrdenado.addAll(auxListadoPerfilesSeccion);
                        auxListadoPerfilesSeccion = new ArrayList();
                    }
                }
            // Ya tenemos el array de perfiles ordenados por seccion y longitud.*/
                
                
                
                // TODO generar primero cabeceras de Carriles y Compensadores y despues en un segundo bucle las de los perfiles
                
                // TODO hacer las lineas 


                Element raiz = document.getDocumentElement();        
                Element itemVer = document.createElement(VERSION);
                Element nodeVerX = document.createElement(VERSION_X);
                nodeVerX.appendChild(document.createTextNode(Integer.toString(version)));
                Element nodeVerY = document.createElement(VERSION_Y);
                nodeVerY.appendChild(document.createTextNode("0"));
                itemVer.appendChild(nodeVerX);
                itemVer.appendChild(nodeVerY);
                raiz.appendChild(itemVer);

                Element itemCab = document.createElement(CABECERA); 
                
                // Cabeceras de barras NO perfiles
                for(Barras np: auxListadoNOPerfiles){
                    datosBarraStock = "";
                    datosBarraStockLen = "";
                    seccionYColor = "";
                    Element itemGrupo = document.createElement(CABECERA_GRUPO);
                    Element nodeCod = document.createElement(CABECERA_GRUPO_CODIGO);
                    String perfilBarra = np.getTipoPerfil();
                    if (maquinaNueva || maquinaNuevaSinPerfiles) {
                        if (np.getTipoPerfil().equals("CARRIL") && np.sePuedeCortarEnMaquinaNueva().equals(1)) {
                           perfilBarra = "CARRIL_INV";
                        } else if (np.getTipoPerfil().equals("CARRILPLUS") && np.sePuedeCortarEnMaquinaNueva().equals(1)){
                            perfilBarra = "CARRILPLUS_INV";
                        }
                    }    
                    nodeCod.appendChild(document.createTextNode(perfilBarra));
                    Element nodeColorI = document.createElement(CABECERA_GRUPO_COLORINT);
                    if (!np.getGrupoStock().equals(-1)){
                        datosBarraStockLen = obtenerLongitudGrupoStock((long)np.getGrupoStock(), listadoBarrasStock);
                        datosBarraStock = datosBarraStockLen + " ("+ (np.getIdentificador_stock()).toString() + ")";
                    } else {
                        indiceArroba = (np.getTagD().length() - np.getTagD().indexOf("@"))-1;
                        datosBarraStockLen = barraCompletaSistema;
                        datosBarraStock = (np.getTagD().substring(np.getTagD().length()-indiceArroba) +"m"); // En no perfiles, no va la seccion aqui
                    }
                    nodeColorI.appendChild(document.createTextNode(datosBarraStock));
                    Element nodeColorE = document.createElement(CABECERA_GRUPO_COLOREXT);
                    seccionYColor = (np.getColor());
                    nodeColorE.appendChild(document.createTextNode(seccionYColor));
                    Element nodeCantidad = document.createElement(CABECERA_GRUPO_CANTIDAD);
                    nodeCantidad.appendChild(document.createTextNode("1"));
                    itemGrupo.appendChild(nodeCod);
                    itemGrupo.appendChild(nodeColorI);
                    itemGrupo.appendChild(nodeColorE);
                    itemGrupo.appendChild(nodeCantidad);
                    itemCab.appendChild(itemGrupo);

                }
                
                // Cabeceras de PERFILES
                //for(int i = 0; i<auxListadoPerfilesOrdenado.size();i++){
                if (!maquinaNuevaSinPerfiles){
                    for (Barras p : auxListadoPerfilesOrdenado){
                        datosBarraStock = "";
                        datosBarraStockLen = barraCompletaSistema;
                        seccionYColor = "";
                        Element itemGrupo = document.createElement(CABECERA_GRUPO);
                        Element nodeCod = document.createElement(CABECERA_GRUPO_CODIGO);
                        nodeCod.appendChild(document.createTextNode(p.getTipoPerfil()));
                        Element nodeColorI = document.createElement(CABECERA_GRUPO_COLORINT);
                        datosBarraStock = ("S"+String.valueOf(p.getPerteneceASeccion()));
                        nodeColorI.appendChild(document.createTextNode(datosBarraStock));
                        Element nodeColorE = document.createElement(CABECERA_GRUPO_COLOREXT);
                        seccionYColor = (p.getColor());
                        nodeColorE.appendChild(document.createTextNode(seccionYColor));
                        Element nodeCantidad = document.createElement(CABECERA_GRUPO_CANTIDAD);
                        nodeCantidad.appendChild(document.createTextNode("1"));
                        itemGrupo.appendChild(nodeCod);
                        itemGrupo.appendChild(nodeColorI);
                        itemGrupo.appendChild(nodeColorE);
                        itemGrupo.appendChild(nodeCantidad);
                        itemCab.appendChild(itemGrupo);

                    }    
                }
                raiz.appendChild(itemCab);

                Element itemA = document.createElement(BARRA); 
                
                // Lineas de las barras NO perfiles
                for(Barras np: auxListadoNOPerfiles){
                    if (maquinaNueva || maquinaNuevaSinPerfiles){
                        switch (np.sePuedeCortarEnMaquinaNueva()){
                            case 0:
                                break;
                            case 1:
                                // debo invertir angulos y recalcular ol e il, ojo si es carril debo cambiar modelo tambien.
                                if (np.equals("CARRIL")){
                                    np.setTipoPerfil("CARRIL_INV");
                                    np.invertirAngulosBarra();
                                } else if (np.equals("CARRILPLUS")){
                                    np.setTipoPerfil("CARRILPLUS_INV");
                                    np.invertirAngulosBarra();
                                } else if (np.equals("COMPENSADOR")){
                                    np.invertirAngulosBarraParaMaquinaX2();// Ya intercambia los ol e il.
                                }    
                                np.setTagD(np.getTagD()!=null ? np.getTagD()+"-X2" : "X2");
                                break;
                        }
                    }
                    //Vamos a simplificar la nomenclatura de nodos     
                    Element itemAA = document.createElement(BARRA_DATOS);
                    Element itemAAA = document.createElement(BARRA_DATOS_TIPO);
                    itemAAA.appendChild(document.createTextNode("TODOCRISTAL"));
                    Element itemAAB = document.createElement(BARRA_DATOS_SISTEMA);
                    itemAAB.appendChild(document.createTextNode(np.getTipoSistema()));
                    Element itemAAC = document.createElement(BARRA_DATOS_CODIGO);
                    itemAAC.appendChild(document.createTextNode(np.getTipoPerfil()));

                    Element itemAAD = document.createElement(BARRA_DATOS_COLORINT);
                    
                    
                        if (!np.getGrupoStock().equals(-1)){
                            datosBarraStockLen = obtenerLongitudGrupoStock((long)np.getGrupoStock(), listadoBarrasStock) ;
                            datosBarraStock = datosBarraStockLen + " ("+ (np.getIdentificador_stock()).toString() + ")";
                        
                        } else {
                                //
                            indiceArroba = (np.getTagD().length() - np.getTagD().indexOf("@"))-1;
                            datosBarraStockLen = barraCompletaSistema;
                            datosBarraStock = (np.getTagD().substring(np.getTagD().length()-indiceArroba) +"m"); // En no perfiles, no va la seccion aqui

                        }
                    
                    itemAAD.appendChild(document.createTextNode(datosBarraStock));

                    Element itemAAF = document.createElement(BARRA_DATOS_COLOREXT);
                    seccionYColor = (np.getColor());
                    itemAAF.appendChild(document.createTextNode(seccionYColor));

                    Element itemAAG = document.createElement(BARRA_DATOS_LONG);
                    //itemAAG.appendChild(document.createTextNode(np.getLongitud().toString()));
                    itemAAG.appendChild(document.createTextNode(datosBarraStockLen));
                    Element itemAAH = document.createElement(BARRA_DATOS_RETAL);
                    itemAAH.appendChild(document.createTextNode("0"));
                    Element itemAAI = document.createElement(BARRA_DATOS_GROSOR);
                    itemAAI.appendChild(document.createTextNode(np.getGrosor().toString()));
                    Element itemAAJ = document.createElement(BARRA_DATOS_CANTIDAD);
                    itemAAJ.appendChild(document.createTextNode("1"));
                    Element itemAAK = document.createElement(BARRA_DATOS_CORTE);
                    Element itemAAKA = document.createElement(BARRA_DATOS_CORTE_ANGIZQUIERDO);   

                    itemAAKA.appendChild(document.createTextNode(np.getAnguloIzquierdo().toString()));
                    Element itemAAKB = document.createElement(BARRA_DATOS_CORTE_ANGDERECHO);              
                    itemAAKB.appendChild(document.createTextNode(np.getAnguloDerecho().toString()));
                    Element itemAAKC = document.createElement(BARRA_DATOS_CORTE_LONGINFERIOR); // A LAS DOS MAQUINAS SOLO MANDAMOS LA SUPERIOR
                    /*if (maquinaNueva && np.getLongitudInterior()!=null){
                        itemAAKC.appendChild(document.createTextNode(np.getLongitudInterior().toString()));
                    } else {
                        itemAAKC.appendChild(document.createTextNode(""));
                    } */   
                    itemAAKC.appendChild(document.createTextNode(""));
                    Element itemAAKD = document.createElement(BARRA_DATOS_CORTE_LONGSUPERIOR);
                    itemAAKD.appendChild(document.createTextNode(np.getLongitudExterior().toString()));
                    Element itemAAKE = document.createElement(BARRA_DATOS_CORTE_CODIGOUNICO);
                    itemAAKE.appendChild(document.createTextNode(np.getIdentificadorUnicoBarra()));

                    Element itemAAKF = document.createElement(BARRA_DATOS_CORTE_ETIQUETA);
                    itemAAKF.appendChild(document.createTextNode(np.getTagA().substring(0,5) + " S:" + np.getPerteneceASeccion()));

                    Element itemAAKG = document.createElement(BARRA_DATOS_CORTE_ETIQUETA);
                    if (np.getTagB() != null && !np.getTagB().equals("")){               
                        itemAAKG.appendChild(document.createTextNode(np.getTagB()));
                    }
                    Element itemAAKH = document.createElement(BARRA_DATOS_CORTE_ETIQUETA);
                    if (np.getTagC() != null && !np.getTagC().equals("")){               
                        itemAAKH.appendChild(document.createTextNode(np.getTagC()));
                    }
                    Element itemAAKI = document.createElement(BARRA_DATOS_CORTE_ETIQUETA);
                    if (np.getTagD() != null && !np.getTagD().equals("")){               
                        itemAAKI.appendChild(document.createTextNode(np.getTagD()));
                    }

                    itemAAK.appendChild(itemAAKA); //Uni�n principal de datos del corte
                    itemAAK.appendChild(itemAAKB);
                    itemAAK.appendChild(itemAAKC);
                    itemAAK.appendChild(itemAAKD);
                    itemAAK.appendChild(itemAAKE);
                    itemAAK.appendChild(itemAAKF);
                    if (np.getTagB() != null && !np.getTagB().equals("")){
                        itemAAK.appendChild(itemAAKG);
                    }  
                    if (np.getTagC() != null && !np.getTagC().equals("")){
                        itemAAK.appendChild(itemAAKH);
                    } 
                    if (np.getTagD() != null && !np.getTagD().equals("")){
                        itemAAK.appendChild(itemAAKI);
                    } 
                    itemAA.appendChild(itemAAA);
                    itemAA.appendChild(itemAAB);
                    itemAA.appendChild(itemAAC);
                    itemAA.appendChild(itemAAD);
                    itemAA.appendChild(itemAAF);
                    itemAA.appendChild(itemAAG);
                    itemAA.appendChild(itemAAH);
                    itemAA.appendChild(itemAAI);
                    itemAA.appendChild(itemAAJ);
                    itemAA.appendChild(itemAAK);
                    itemA.appendChild(itemAA);
                }        
                
                if (!maquinaNuevaSinPerfiles) {
                    for (Barras p : auxListadoPerfilesOrdenado) {
                        //Vamos a simplificar la nomenclatura de nodos     
                        Element itemAA = document.createElement(BARRA_DATOS);
                        Element itemAAA = document.createElement(BARRA_DATOS_TIPO);
                        itemAAA.appendChild(document.createTextNode("TODOCRISTAL"));
                        Element itemAAB = document.createElement(BARRA_DATOS_SISTEMA);
                        itemAAB.appendChild(document.createTextNode(p.getTipoSistema()));
                        Element itemAAC = document.createElement(BARRA_DATOS_CODIGO);
                        itemAAC.appendChild(document.createTextNode(p.getTipoPerfil()));

                        Element itemAAD = document.createElement(BARRA_DATOS_COLORINT);
                        datosBarraStock = ("S" + String.valueOf(p.getPerteneceASeccion()));
                        itemAAD.appendChild(document.createTextNode(datosBarraStock));

                        Element itemAAF = document.createElement(BARRA_DATOS_COLOREXT);
                        seccionYColor = (p.getColor());
                        itemAAF.appendChild(document.createTextNode(seccionYColor));

                        Element itemAAG = document.createElement(BARRA_DATOS_LONG);
                        itemAAG.appendChild(document.createTextNode(barraCompletaSistema));
                        Element itemAAH = document.createElement(BARRA_DATOS_RETAL);
                        itemAAH.appendChild(document.createTextNode("0"));
                        Element itemAAI = document.createElement(BARRA_DATOS_GROSOR);
                        itemAAI.appendChild(document.createTextNode(p.getGrosor().toString()));
                        Element itemAAJ = document.createElement(BARRA_DATOS_CANTIDAD);
                        itemAAJ.appendChild(document.createTextNode("1"));
                        Element itemAAK = document.createElement(BARRA_DATOS_CORTE);
                        Element itemAAKA = document.createElement(BARRA_DATOS_CORTE_ANGIZQUIERDO);

                        itemAAKA.appendChild(document.createTextNode(p.getAnguloIzquierdo().toString()));
                        Element itemAAKB = document.createElement(BARRA_DATOS_CORTE_ANGDERECHO);
                        itemAAKB.appendChild(document.createTextNode(p.getAnguloDerecho().toString()));
                        Element itemAAKC = document.createElement(BARRA_DATOS_CORTE_LONGINFERIOR);
                        //itemAAKC.appendChild(document.createTextNode(listadoBarrasSistema.get(i).getLongitudInterior().toString()));
                        itemAAKC.appendChild(document.createTextNode(""));
                        Element itemAAKD = document.createElement(BARRA_DATOS_CORTE_LONGSUPERIOR);
                        itemAAKD.appendChild(document.createTextNode(p.getLongitudExterior().toString()));
                        Element itemAAKE = document.createElement(BARRA_DATOS_CORTE_CODIGOUNICO);
                        itemAAKE.appendChild(document.createTextNode(p.getIdentificadorUnicoBarra()));

                        Element itemAAKF = document.createElement(BARRA_DATOS_CORTE_ETIQUETA);
                        itemAAKF.appendChild(document.createTextNode(p.getTagA().substring(0, 5) + " S:" + p.getPerteneceASeccion()));

                        Element itemAAKG = document.createElement(BARRA_DATOS_CORTE_ETIQUETA);
                        if (p.getTagB() != null && !p.getTagB().equals("")) {
                            itemAAKG.appendChild(document.createTextNode(p.getTagB()));
                        }
                        Element itemAAKH = document.createElement(BARRA_DATOS_CORTE_ETIQUETA);
                        if (p.getTagC() != null && !p.getTagC().equals("")) {
                            itemAAKH.appendChild(document.createTextNode(p.getTagC()));
                        }
                        Element itemAAKI = document.createElement(BARRA_DATOS_CORTE_ETIQUETA);
                        if (p.getTagD() != null && !p.getTagD().equals("")) {
                            itemAAKI.appendChild(document.createTextNode(p.getTagD()));
                        }

                        itemAAK.appendChild(itemAAKA); //Uni�n principal de datos del corte
                        itemAAK.appendChild(itemAAKB);
                        itemAAK.appendChild(itemAAKC);
                        itemAAK.appendChild(itemAAKD);
                        itemAAK.appendChild(itemAAKE);
                        itemAAK.appendChild(itemAAKF);
                        if (p.getTagB() != null && !p.getTagB().equals("")) {
                            itemAAK.appendChild(itemAAKG);
                        }
                        if (p.getTagC() != null && !p.getTagC().equals("")) {
                            itemAAK.appendChild(itemAAKH);
                        }
                        if (p.getTagD() != null && !p.getTagD().equals("")) {
                            itemAAK.appendChild(itemAAKI);
                        }
                        itemAA.appendChild(itemAAA);
                        itemAA.appendChild(itemAAB);
                        itemAA.appendChild(itemAAC);
                        itemAA.appendChild(itemAAD);
                        itemAA.appendChild(itemAAF);
                        itemAA.appendChild(itemAAG);
                        itemAA.appendChild(itemAAH);
                        itemAA.appendChild(itemAAI);
                        itemAA.appendChild(itemAAJ);
                        itemAA.appendChild(itemAAK);
                        itemA.appendChild(itemAA);
                    }
                }
                raiz.appendChild(itemA);  
                
                int cont = 0;
                while(cont < auxlistadoBarrasSistema.size()-1 && auxlistadoBarrasSistema.get(cont).getIdentificadorUnicoBarra().subSequence(0, 5).toString().equals("RESTO")){
                    cont++;
                }
                return auxlistadoBarrasSistema.get(cont).getIdentificadorUnicoBarra().subSequence(0, 5).toString()+ (auxVariosColores ? "_"+ barraServices.colorAluminio(auxlistadoBarrasSistema): "")+"_C.xml";
            }
        } else {
            return String.format("%02d", auxlistadoBarrasSistema.get(0).getIdentificadorUnicoBarra().substring(0, 5)) +"NULLC.xml";
        }
        return String.format("%02d", auxlistadoBarrasSistema.get(0).getIdentificadorUnicoBarra().substring(0, 5)) +"NULLC.xml";
    }
    
    
    public void guardarArchivo(String referencia, Integer version, Boolean desdeArchivoTxt) throws ParserConfigurationException, Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation implementation = builder.getDOMImplementation();

        List<List<Barras>> barrasNecesidadColores = new ArrayList<>();
        Boolean variosColores = Boolean.FALSE;
        String sinColor = "    ";
        List<String> colores = new ArrayList<>();
        ArrayList<String> tipoPerfilQueImpidenCorteX2 = new ArrayList<>();

        if (desdeArchivoTxt) {
            this.listadoBarrasSistema = new FileUtil().barrasDesdeFicheroTxt(referencia);
        } else {
            this.listadoBarrasSistema = barraServices.getBarrasByProyecto(referencia);
        }

        if (listadoBarrasSistema != null && !listadoBarrasSistema.isEmpty()) {
            for (Barras b : listadoBarrasSistema) {
                if (!colores.contains(b.getColor()) && !b.getIdentificadorUnicoBarra().contains("REST") && !b.getColor().equals(sinColor)) {
                    colores.add(b.getColor());
                }
            }

            if (colores.size() >= 1) {
                if (colores.size() > 1) {
                    variosColores = Boolean.TRUE;
                }
                for (int i = 0; i < colores.size(); i++) {
                    barrasNecesidadColores.add(i, new ArrayList<Barras>());
                    for (Barras b : listadoBarrasSistema) {
                        if (b.getColor().equals(colores.get(i)) || b.getColor().equals(sinColor)) {
                            barrasNecesidadColores.get(i).add(b);
                        }
                    }
                    Document document = implementation.createDocument(null, "JOB", null);
                    document.setXmlVersion("1.0");
                    
                    
                    
                    // Con que una barra no se pueda cortar en la maquina, ya no se manda. Por eso solo comprobamos si sigue siendo true, en el momento que sea false ya no validamos de nuevo y mandaremos solo el corte a la maquina antigua
                    Integer sePuedeMandarAMaquinCorteNueva = 0;
                    String codigoMaquinaPosible = "_X1"; // Inicializo a solo maquina antigua
                    if (listadoBarrasSistema!=null){
                        for (Barras ba: listadoBarrasSistema){
                           
                                switch (ba.sePuedeCortarEnMaquinaNueva()){
                                    case 0:  // Se puede sin problema
                                        break;
                                    case 1: // se ha de invertir la barra
                                        break;
                                    case -1:
                                     if (!tipoPerfilQueImpidenCorteX2.contains(ba.getTipoPerfil())){
                                            tipoPerfilQueImpidenCorteX2.add(ba.getTipoPerfil());
                                        }
                                    break;
                                }
                                
                                /*if (!sePuedeMandarAMaquinCorteNueva){
                                    break;
                                }*/
                           
                        }  
                    }
                    switch (tipoPerfilQueImpidenCorteX2.size()){
                        case 0:
                           sePuedeMandarAMaquinCorteNueva = 0;
                           break;
                        case 1:
                            if (tipoPerfilQueImpidenCorteX2.get(0).startsWith("PER")){
                                // Se puede cortar solo carriles y compensadores
                                sePuedeMandarAMaquinCorteNueva = 1;
                            }
                            break;
                        default:
                            sePuedeMandarAMaquinCorteNueva = -1;
                            break;    
                    }
                    
                    //
                    Source source = new DOMSource(document);
                    //Indicamos donde lo queremos almacenar
                    String ficheroCreado = generarXMLCortes(referencia, document, version, barrasNecesidadColores.get(i), variosColores, -1);
                    ficheroCreado = ficheroCreado.replace("_C", codigoMaquinaPosible +"_C_TMP"); // NUEVA GESTION DE VERSIONES
                    Result resultXMLCortes = new StreamResult(new java.io.File(OUTPUT_FOLDER_TRONZADORA.concat(ficheroCreado)));
                    //+(proyecto.getIdNav()!=null ? proyecto.getIdNav() : letra + proyecto.getId().toString().substring(1))+" (T"+String.format("%02d", proyecto.getVersion())+").xml")); //nombre del archivo
                    Transformer transformerXMLCortes = TransformerFactory.newInstance().newTransformer();
                    transformerXMLCortes.transform(source, resultXMLCortes);
                    //Fin Generador de XML Tronzadora
                    // Ahora vamos a generar al x2 si es posible
                    
                    if (sePuedeMandarAMaquinCorteNueva>=0){
                        codigoMaquinaPosible = "_X2"; // Se puede mandar a las dos maquinas
                        Document documentX2 = implementation.createDocument(null, "JOB", null);
                        documentX2.setXmlVersion("1.0");
                        Source sourceX2 = new DOMSource(documentX2);                
                        //ALMACENAMIENTO Y NOMBRE DEL ARCHIVO
                        String ficheroCreadoX2 = generarXMLCortes(referencia, documentX2, version, barrasNecesidadColores.get(i), variosColores, sePuedeMandarAMaquinCorteNueva);
                        ficheroCreadoX2 = ficheroCreadoX2.replace("_C", codigoMaquinaPosible +"_C_TMP");
                        Result resultXMLCortesX2 = new StreamResult(new java.io.File(OUTPUT_FOLDER_TRONZADORA.concat(ficheroCreadoX2)));
                        Transformer transformerXMLCortesX2 = TransformerFactory.newInstance().newTransformer();
                        transformerXMLCortesX2.transform(sourceX2, resultXMLCortesX2);
                    }
                    
                    System.out.println("XML CORTES TRONZADORA GENERADO CORRECTO, IDPROYECTO: " + referencia);
                }
            }
            
        } else {
            System.out.println("NO HAY BARRAS PARA GENERAR UN FICHERO DE CORTE: " + referencia);
        }
    }
    
    private String obtenerLongitudGrupoStock(Long grupo, List<StockBarras> listadoBarras){
        //StockBarras grupoSeleccionado = null;
        if (grupo != 0){
            for(StockBarras sb : listadoBarras){
                if(grupo.equals(sb.getId())){ 
                    return String.valueOf(sb.getLongitud());
                }
            }        
        }    
        return String.valueOf(listadoBarras!=null && !listadoBarras.isEmpty() && listadoBarras.get(0).getTipoSistema().contains("INFINIA") ? 7000.0 : 6300.0);
    }
    /**
     * Permite agrupar todas las barras para el XML de corte.
     * Los perfiles(Incluidos los restos) se agrupan por TIPO PERFIL, GRUPO STOCK, IDENTIFICADOR STOCK.
     * Comentario: Esta linea es necesaria mientras los restos sean GRUPO STOCK = 0 (&& (d.getGrupoStock().equals(e.getGrupoStock()) || d.getGrupoStock().equals(0) || e.getGrupoStock().equals(0)))
     * @param listadoBarrasSistema
     * @return 
     */
    private List<Barras> agruparBarrasStock(List<Barras> listadoBarrasSistema){
        Integer auxLongitud=0;
        String auxTipoPerfil="";
        Integer auxIdentificadorGrupo=0;
        Integer grupoStock=0;
        Integer identificadorStock=0;
        
        Collections.sort(listadoBarrasSistema);        
        
        List<String> ordenPerfiles = new ArrayList<String>();
        for(Barras b : listadoBarrasSistema){
            if(!ordenPerfiles.contains(b.getTipoPerfil())){
                ordenPerfiles.add(b.getTipoPerfil());
            }
        }
        List<Barras> auxOrdenadoNecesidad = new ArrayList<>();        
        for(String perfil : ordenPerfiles){
            for(Barras c : listadoBarrasSistema){
                if(c.getTipoPerfil().equals(perfil)){
                    auxOrdenadoNecesidad.add(c);
                }
            }
        }
        List<Barras> auxAgrupadoGrupoStock = new ArrayList<>();
            
        for(Barras d : auxOrdenadoNecesidad){
            //grupoStock = d.getGrupoStock();
            //identificadorStock = d.getIdentificador_stock();
            if(!auxAgrupadoGrupoStock.contains(d)){
                auxAgrupadoGrupoStock.add(d);

                for(Barras e : auxOrdenadoNecesidad){   
                    if(d.getTipoPerfil().equals(e.getTipoPerfil()) && !d.getIdentificadorUnicoBarra().equals(e.getIdentificadorUnicoBarra()) 
                            && (d.getGrupoStock().equals(e.getGrupoStock()) || d.getGrupoStock().equals(0) || e.getGrupoStock().equals(0)) //Ver comentario*
                            && d.getIdentificador_stock().equals(e.getIdentificador_stock()) 
                            && !auxAgrupadoGrupoStock.contains(e)){
                        auxAgrupadoGrupoStock.add(e);
                    }                    
                }
            }
        }
        
        return auxAgrupadoGrupoStock;
        
    }
    
   
   
}
