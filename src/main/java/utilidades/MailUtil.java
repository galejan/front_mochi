
package utilidades;

import com.todocristal.fabrica.webservice.model.StockBarras;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 * @author rafael
 */
public class MailUtil {
  
    Boolean bloquearEnvio = Boolean.FALSE;  
    private Integer MINIMOUTILIZABLE = 1000;
    private static String nombreRal = "";
    private final static String OUTPUT_FOLDER_TRONZADORA ="/home/ftp/tronzadora/";
    // Etiquetas de XML para mï¿½quina de cortes
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
    private final static String BARRA_DATOS_CANTIDAD = "MLT"; //Siempre debe ser 1 por diseï¿½o tronzadora
    private final static String BARRA_DATOS_CORTE = "CUT";
    private final static String BARRA_DATOS_CORTE_ANGIZQUIERDO = "ANGL";
    private final static String BARRA_DATOS_CORTE_ANGDERECHO = "ANGR";
    private final static String BARRA_DATOS_CORTE_LONGINFERIOR = "IL";
    private final static String BARRA_DATOS_CORTE_LONGSUPERIOR = "OL";
    private final static String BARRA_DATOS_CORTE_CODIGOUNICO = "BCOD";  //No debe haber dos cï¿½digos iguales.
    private final static String BARRA_DATOS_CORTE_ETIQUETA = "LBL";
    
    private final Logger log = Logger.getLogger(this.getClass().getName());  
    
    //Desactivamos el correo de aviso de los colores, hay que introducir en este array los colores si queremos activarlo de nuevo;
    public List<String> coloresAviso = new ArrayList<String>();
     
    
    public HtmlEmail inicializaMail(String mailDestino, String nombreUsuarioDestino){
        
        HtmlEmail email = new HtmlEmail();

        try{
            if(mailDestino == null && mailDestino != ""){
                throw new EmailException("MAIL NO ENVIADO: Variable mail estÃ¡ en blanco");
            }
            email.setHostName("smtp.gmail.com");
            email.setFrom("soporte@todocristal.eu", "Servidor");
            email.setSmtpPort(587);
            email.setAuthentication("soporte@todocristal.eu", "xizmjhcohtxpqxjf");
            email.setSSL(false);
            email.setTLS(true);
            email.setCharset("UTF-8");
            
            email.addTo(mailDestino, nombreUsuarioDestino);
            
            return email;
        } catch (EmailException eEx){
            log.log(Level.SEVERE, eEx.getMessage());
        }
        
        return null;
    }
    
    
    public ArrayList<StockBarras> BarrasParaLacarSegunConsumo(String cadenaConsumo, String tipoSistema){
        // parsear la cadena
        // crear barras para el fichero de corte con un nuevo constructor ya que los angulos seran rectos, etc...
        ArrayList<StockBarras> barrasPedidoLacado = new ArrayList<>();
       
            
    ArrayList<String> consumo = new ArrayList<>();
    String[] grupos = cadenaConsumo.split("@"); // Dividir por grupos de color
    
    for (String grupo : grupos) {
        grupo = grupo.trim();
        if (grupo.isEmpty()) continue;
        
        // Extraer código de color EN ESTE CASO SETEAMOS PORQUE ES PARA EL FICHERO DE CORTE DE LACADO
        String color = "CRUDO";
        if (color == null) continue; // Si no hay código válido, saltar grupo
        
        // Procesar elementos del grupo
        String[] elementos = grupo.split("\\|");
        for (String elemento : elementos) {
            elemento = elemento.trim();
            if (elemento.startsWith("(")) continue; // Saltar encabezado de color
            
            if (!elemento.isEmpty()) {
                barrasPedidoLacado.addAll(procesarElemento(elemento, color, tipoSistema));
            }
        }
    
    }
    
    return barrasPedidoLacado;
}

private String extraerCodigoColor(String grupo) {
    int inicio = grupo.indexOf("[");
    int fin = grupo.indexOf("]");
    if (inicio != -1 && fin != -1 && fin > inicio) {
        return grupo.substring(inicio + 1, fin);
    }
    return null;
}

private ArrayList<StockBarras> procesarElemento(String elemento, String color, String tipoSistema) {
    
    ArrayList<StockBarras> barras = new ArrayList<>();
    String[] partesPerfil = elemento.split("-", 2); // Dividir en perfil y especificación
    if (partesPerfil.length != 2) return null;
    
    String perfil = partesPerfil[0];
    String especificacion = partesPerfil[1];
    
    // Dividir cantidad y longitud
    String[] partesEspec = especificacion.split("x");
    if (partesEspec.length != 2) return null;
    
    String cantidad = partesEspec[0];
    String longitud = partesEspec[1];
    
    for (int i=0; i<Integer.valueOf(cantidad); i++){
        barras.add(new StockBarras(perfil,color,Double.valueOf(longitud),tipoSistema));
    }    
    // Construir cadena final
    
    //consumo.add(perfil + "-" + cantidad + "-" + longitud + "-" + color);
    return barras;
}
        
    
    
    /**
     * Notificación al departamento de compras con las barras disponibles para lacar. 
     * Sólo son barras para lacar el proyecto que no sea XXXX.
     * 
     * @param barrasLacado
     * @param barrasLacadoNecesidad
     * @param nombreproyecto
     * @param colorProyecto Es el color del lacado (definitivo) en el que quedarán las barras
     * @return 
     */
     public Boolean notificarBarrasParaLacar(List<StockBarras> barrasLacado, List<Double> barrasLacadoNecesidad, String nombreproyecto, String colorProyecto, String auxLogLacado, 
             String auxLacadoPerfiles, String seccionesEditadas, String auxLacadoForzado, List<String> perfilesNoMochilificados, String consumo, String otrosLacables) throws EmailException, IOException{
        //TODO para pruebas ponemos siempre departamento Informatica
        
        //HtmlEmail email = inicializaMail("agarcia@todocristal.eu", "email en desarrollo");
        HtmlEmail email = inicializaMail("lacados@todocristal.eu", "Departamento técnico");
        try{
           
            email.setSubject("APPFABRICA02 PEDIDOLACADO 2.0 " + nombreproyecto);
            
            String cadena ="";
            String cadenaFicheroLacado = "DESGLOSE DE BARRAS.";
            ArrayList<String> arrayCadenaFicheroLacado = new ArrayList<>();
            arrayCadenaFicheroLacado.add(cadenaFicheroLacado);
            String lineaSinCorchetes = "", auxSeccionesEditadas = "";
            Boolean esLacadoEnteras = Boolean.FALSE;
            ArrayList<Integer> yaContados = new ArrayList<>();
            Integer cont=0, contadorPerfil=0;
            Double longitudI, longitudJ=0.0;
            String perfil = "";
            Integer barrasPerfil = 0;
            Boolean esCorteBarraCompleta = Boolean.FALSE;
            Boolean generaResto = Boolean.FALSE;
            int i,j = 0;
            Double longitudTotalPerfil= 0.0;
            Double longitudTotalCarril = 0.0; // La usamos para el mensaje de advertencia
            Double longitudTotalBarraTipoPerfil = 0.0; //La usamos para el mensaje de advertencia
            String avisoTotalPerfilMenorQueCarril = ""; 
            String nombreFicheroLacado = "";
            String referencia = extraeReferenciaNombreProyecto(nombreproyecto);
            ArrayList<StockBarras> barrasPedidoLacado = new ArrayList<>();
            Double longitudBarraCompletaSegunSistema = 6300.0; // inicializo al defecto.
            
            if (barrasLacado!=null && !barrasLacado.isEmpty()){
                esLacadoEnteras = barrasLacado.get(0).getColor().equals("CRUSOP");
                String tipoSistema = barrasLacado.get(0).getTipoSistema();
                longitudBarraCompletaSegunSistema = (tipoSistema.contains("INFINIA") ? 7000.0 : 6300.0);
                barrasPedidoLacado.addAll(BarrasParaLacarSegunConsumo(consumo, tipoSistema));
            }
            String tagBarraCompletaSistema = String.valueOf(longitudBarraCompletaSegunSistema);
            
            for (i=0; i<barrasLacado.size(); i++){
                if (!yaContados.contains(i)){
                    //barrasPedidoLacado.add(barrasLacado.get(i));
                    perfil = barrasLacado.get(i).getTipoPerfil();
                    barrasPerfil=0;
                    esCorteBarraCompleta=(barrasLacado.get(i).getLongitud().equals(longitudBarraCompletaSegunSistema));
                    if (esCorteBarraCompleta){
                        longitudI = barrasLacadoNecesidad.get(i);
                        
                        if ((barrasLacado.get(i).getLongitud()-barrasLacadoNecesidad.get(i))>=MINIMOUTILIZABLE){
                           generaResto = (esLacadoEnteras ? Boolean.FALSE : Boolean.TRUE);
                        }
                    } else {
                        longitudI = barrasLacado.get(i).getLongitud();
                        if (barrasLacado.get(i).getLongitud()-barrasLacadoNecesidad.get(i) >= MINIMOUTILIZABLE){
                            generaResto = (esLacadoEnteras? Boolean.FALSE : Boolean.TRUE);
                        }
                    }
                    yaContados.add(i);
                    cont = 1;
                    for (j=i+1; j<barrasLacado.size(); j++){
                        if (barrasLacado.get(j).getLongitud().equals(longitudBarraCompletaSegunSistema) && ((barrasLacado.get(j).getLongitud()-barrasLacadoNecesidad.get(j))>MINIMOUTILIZABLE)){
                            longitudJ = barrasLacadoNecesidad.get(j);
                            
                        } else {
                            longitudJ = barrasLacado.get(j).getLongitud();
                        }
                        if (barrasLacado.get(j).getTipoPerfil().equals(perfil) && longitudJ.equals(longitudI)&& !yaContados.contains(j) && !generaResto){
                            cont++;
                            yaContados.add(j);
                        }
                    }
                    if (perfil.contains("PERF") || perfil.contains("PEROD") || perfil.contains("PER10CR") || perfil.contains("PER10MIC") || perfilesNoMochilificados.contains(perfil)){
                       
                    } else {
                        if (esLacadoEnteras){
                                cadena += "<b>[ ] " + cont + " x " + perfil + " "+tagBarraCompletaSistema+" <br>";
                                
                            } else {
                                cadena += "<b>[ ] " + cont + " x " + perfil + " ";
                                cadenaFicheroLacado = "<b> [ ] " + cont + " x " + perfil + " "; // Aqui no lo meto aun en el array de cadenas porque se esta terminando de rellenar
                                if (generaResto){  //para generar resto el corte debe ser barra completa
                                    cadena += String.format("%1$.1f",longitudI) + " ("+tagBarraCompletaSistema+") Resto: " + (grupoMenorLongitud(longitudBarraCompletaSegunSistema-barrasLacadoNecesidad.get(i))) + "</b><br>" ;
                                   
                                } else {
                                    if (esCorteBarraCompleta){
                                        cadena += " ("+tagBarraCompletaSistema+") " + "</b><br>" ;
                                        
                                    } else {
                                        cadena += (esGrupo(longitudI)  ? "("+longitudI+")":String.format("%1$.1f",longitudI)) + "</b><br>" ;
                                        
                                    }
                                }
                            }
                    }
                    esCorteBarraCompleta = Boolean.FALSE;
                    generaResto = Boolean.FALSE;
                    
                }
            }
            cadena += auxLacadoPerfiles + "<br>";  // Insertamos aqui el desglose de lacado de perfiles
            ArrayList<String> lineasPerfiles = new ArrayList<>();
            StringTokenizer tokens = new StringTokenizer(cadena, "<br>");
                while(tokens.hasMoreTokens()){
                    String s = (tokens.nextToken().replace("<b>",""));
                    s.replace("</b>", "");
                    if (s.contains("("+tagBarraCompletaSistema+")")){
                        s = s.substring(0, s.indexOf("("+tagBarraCompletaSistema+")"));
                    }
                    if (s.startsWith("[")){
                        lineasPerfiles.add(s);
                    }
                    
                }
                                   
            arrayCadenaFicheroLacado.addAll(lineasPerfiles);
            yaContados = new ArrayList<>();
            contadorPerfil = 0;
            for (i=0; i<barrasLacado.size(); i++){
                longitudTotalPerfil = 0.0;
                contadorPerfil = 0;
                if (!yaContados.contains(i)){
                    perfil = barrasLacado.get(i).getTipoPerfil();
                    longitudTotalPerfil += (esLacadoEnteras 
                                                     ? ((perfil.startsWith("PERF") || perfil.contains("PEROD") || perfil.contains("PER10CR") || perfil.contains("PER10MIC") || perfilesNoMochilificados.contains(perfil))
                                                            ? ((int)(barrasLacado.get(i).getLongitud()/longitudBarraCompletaSegunSistema)+((barrasLacado.get(i).getLongitud()%longitudBarraCompletaSegunSistema)>0?1:0))*longitudBarraCompletaSegunSistema 
                                                            + (((barrasLacado.get(i).getLongitud()%longitudBarraCompletaSegunSistema)>0?1:0))*200
                                                            : longitudBarraCompletaSegunSistema) 
                                                     :(barrasLacado.get(i).getLongitud()-barrasLacadoNecesidad.get(i)<MINIMOUTILIZABLE? barrasLacado.get(i).getLongitud() :barrasLacadoNecesidad.get(i)));
                    contadorPerfil++;
                    yaContados.add(i);
                    for (j=i+1; j<barrasLacado.size(); j++){
                        if (barrasLacado.get(j).getTipoPerfil().equals(perfil) && !yaContados.contains(j)){
                            longitudTotalPerfil += ( esLacadoEnteras? longitudBarraCompletaSegunSistema : (barrasLacado.get(j).getLongitud()-barrasLacadoNecesidad.get(j)<MINIMOUTILIZABLE? barrasLacado.get(j).getLongitud() :barrasLacadoNecesidad.get(j)));
                            yaContados.add(j);
                        }
                    }
                                        
                    cadena += "<b> Total " + perfil + ", " + Math.round(longitudTotalPerfil) + "<br>";
                    cadenaFicheroLacado = "<b> Total " + perfil + ", " + Math.round(longitudTotalPerfil) + "<br>";
                    arrayCadenaFicheroLacado.add((cadenaFicheroLacado.replace("<br>", ".")).replace("<b>",""));
                    if(perfil.contains("CARRI")){
                        longitudTotalCarril = longitudTotalPerfil;
                    } else if (perfil.contains("PERF") || perfil.contains("PEROD") || perfil.contains("PER10CR") || perfil.contains("PER10MIC") || perfilesNoMochilificados.contains(perfil)){
                        longitudTotalBarraTipoPerfil = longitudTotalPerfil;
                    }
                }
            }
            
            if (longitudTotalBarraTipoPerfil < longitudTotalCarril){
                avisoTotalPerfilMenorQueCarril = " - EL TOTAL DE PERFIL ES INFERIOR AL CARRIL. COMPROBAD NUMERO DE PERFILES (PUEDE SER POR CIERRE LATERAL, TEJUELO, PLT, PU, ETC..)";
            }
            
            if (!seccionesEditadas.isEmpty()){
                lineaSinCorchetes = seccionesEditadas.substring(1,seccionesEditadas.length()-1);
                StringTokenizer tokens1 = new StringTokenizer (lineaSinCorchetes, ",");
                auxSeccionesEditadas += "<b>CORTE DE SECCIONES EDITADAS MANUALMENTE. <br>";
                while(tokens1.hasMoreTokens()){
                    auxSeccionesEditadas +=("<b> [ ] Seccion " + tokens1.nextToken() + " cortes manuales.<br>");
                }
            }
            
            if (!barrasPedidoLacado.isEmpty()){
                if (esLacadoEnteras){
                    nombreFicheroLacado = ("NO HAY FICHERO DE LACADO. SON BARRAS ENTERAS");
                }   else {
                    try {
                        nombreFicheroLacado = crearFicheroCorteCrudo(referencia, barrasPedidoLacado);
                    } catch (ParserConfigurationException ex) {
                        Logger.getLogger(MailUtil.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (TransformerException ex) {
                        Logger.getLogger(MailUtil.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            
            email.setHtmlMsg("<html>La aplicación de fábrica le transmite la siguiente información: <br><br>"
                + "<b>"+ nombreproyecto + " " + barrasLacado.get(0).getTipoSistema() + " - " + colorProyecto + "<br><br>PEDIDO DE LACADO <br><br>OBSERVACIONES:<br>" 
                    + (!auxLacadoForzado.equals("NO") ? "(FORZADO "+auxLacadoForzado+")":"") + (esLacadoEnteras?" - BARRAS COMPLETAS<br>":"") 
                    + (!seccionesEditadas.isEmpty()? "- LAS SECCIONES " + seccionesEditadas + " NO SE INCLUYEN AL ESTAR EDITADAS MANUALMENTE<br>": "") 
                    + (!avisoTotalPerfilMenorQueCarril.isEmpty()? avisoTotalPerfilMenorQueCarril + "<br>" :"") 
                    + (auxLogLacado.contains("NOTA: El pedido contiene PLT") ? "- EL PROYECTO TIENE PLT O PU. VER HOJA DE PRODUCCION.<br>":"" )
                    + "</b><br><br>" 
                    + cadena  + "<br>" + "<br><b>" +"DESGLOSE DE CORTE <br>"+ auxLogLacado + "<br>"   
                    + (!otrosLacables.isEmpty() ? "<b>OTROS ELEMENTOS A LACAR</b><br>" + otrosLacables + "<br>": "")  
                    + (consumo!=null && !consumo.isEmpty() ? "<b>LineaConsumo = "+consumo +"<br>" : "")        
                    + "</b> " + (!auxSeccionesEditadas.isEmpty()? ("<br>"+ auxSeccionesEditadas) : "") + "<br>" 
                    + crearFichero(nombreproyecto, arrayCadenaFicheroLacado, colorProyecto) + "<br>" 
                    + "Fichero de lacado en maquina x2: " + nombreFicheroLacado +  " </html>");
            
            
            if(!bloquearEnvio){
                email.send();
            } 
            
            
        } catch (EmailException eEx){
            eEx.printStackTrace();
        }
        return false;
    }
     
    private String extraeReferenciaNombreProyecto(String nombreProyecto){
        String auxReferencia = nombreProyecto;
        if (auxReferencia!=null){
            auxReferencia = auxReferencia.replace("D_","");
            auxReferencia = auxReferencia.replace("d_","");
            auxReferencia = auxReferencia.substring(0,5);
        }
        return auxReferencia;
    }
     
     private static String crearFicheroCorteCrudo(String referencia, List<StockBarras> barrasLacado) throws ParserConfigurationException, TransformerConfigurationException, TransformerException{
         String ficheroCreado = "";
         if (barrasLacado!=null && !barrasLacado.isEmpty()){
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMImplementation implementation = builder.getDOMImplementation();
            Document document = implementation.createDocument(null, "JOB", null);
            document.setXmlVersion("1.0");
            Source source = new DOMSource(document);
                    //Indicamos donde lo queremos almacenar
                    ficheroCreado = generarFicheroCorteCrudo(referencia, document,barrasLacado);
                    Result resultXMLCortes = new StreamResult(new java.io.File(OUTPUT_FOLDER_TRONZADORA.concat(ficheroCreado)));
                    //+(proyecto.getIdNav()!=null ? proyecto.getIdNav() : letra + proyecto.getId().toString().substring(1))+" (T"+String.format("%02d", proyecto.getVersion())+").xml")); //nombre del archivo
                    Transformer transformerXMLCortes = TransformerFactory.newInstance().newTransformer();
                    transformerXMLCortes.transform(source, resultXMLCortes);
             
         }
         return ficheroCreado;
     }
     
     private static String generarFicheroCorteCrudo(String referencia, Document document,List<StockBarras> barrasLacado){
         if (barrasLacado!=null && !barrasLacado.isEmpty()){
             Element raiz = document.getDocumentElement();        
                Element itemVer = document.createElement(VERSION);
                Element nodeVerX = document.createElement(VERSION_X);
                nodeVerX.appendChild(document.createTextNode("0"));
                Element nodeVerY = document.createElement(VERSION_Y);
                nodeVerY.appendChild(document.createTextNode("0"));
                itemVer.appendChild(nodeVerX);
                itemVer.appendChild(nodeVerY);
                raiz.appendChild(itemVer);

                Element itemCab = document.createElement(CABECERA); 
             // Primer recorrido cabecera   
             for (StockBarras sb : barrasLacado){
                    Element itemGrupo = document.createElement(CABECERA_GRUPO);
                    Element nodeCod = document.createElement(CABECERA_GRUPO_CODIGO);
                    String tipoPerfilCabecera = sb.getTipoPerfil().replace("-RESTO","");
                    nodeCod.appendChild(document.createTextNode(tipoPerfilCabecera));
                    Element nodeColorI = document.createElement(CABECERA_GRUPO_COLORINT);
                    nodeColorI.appendChild(document.createTextNode(sb.getLongitud().toString()));
                    Element nodeColorE = document.createElement(CABECERA_GRUPO_COLOREXT);
                    nodeColorE.appendChild(document.createTextNode("CRUDO"));
                    Element nodeCantidad = document.createElement(CABECERA_GRUPO_CANTIDAD);
                    nodeCantidad.appendChild(document.createTextNode("1"));
                    itemGrupo.appendChild(nodeCod);
                    itemGrupo.appendChild(nodeColorI);
                    itemGrupo.appendChild(nodeColorE);
                    itemGrupo.appendChild(nodeCantidad);
                    itemCab.appendChild(itemGrupo);
             }
              raiz.appendChild(itemCab);

            Element itemA = document.createElement(BARRA); 
            // Segundo bucle para el cuerpo
            int contBarras = 0;
            for (StockBarras sb2 : barrasLacado){
                contBarras++;
                
                    Element itemAA = document.createElement(BARRA_DATOS);
                    Element itemAAA = document.createElement(BARRA_DATOS_TIPO);
                    itemAAA.appendChild(document.createTextNode("TODOCRISTAL"));
                    Element itemAAB = document.createElement(BARRA_DATOS_SISTEMA);
                    itemAAB.appendChild(document.createTextNode(sb2.getTipoSistema()));
                    Element itemAAC = document.createElement(BARRA_DATOS_CODIGO);
                    Boolean esResto = sb2.getTipoPerfil().contains("-RESTO");
                    String tipoPerfilBody = sb2.getTipoPerfil().replace("-RESTO","");
                    itemAAC.appendChild(document.createTextNode(tipoPerfilBody));
                    Element itemAAD = document.createElement(BARRA_DATOS_COLORINT);
                    itemAAD.appendChild(document.createTextNode(sb2.getLongitud().toString()));
                    Element itemAAF = document.createElement(BARRA_DATOS_COLOREXT);
                    itemAAF.appendChild(document.createTextNode("CRUDO"));
                    Element itemAAG = document.createElement(BARRA_DATOS_LONG);
                    itemAAG.appendChild(document.createTextNode(sb2.getLongitud().toString()));
                    Element itemAAH = document.createElement(BARRA_DATOS_RETAL);
                    itemAAH.appendChild(document.createTextNode("0"));
                    Element itemAAI = document.createElement(BARRA_DATOS_GROSOR);
                    itemAAI.appendChild(document.createTextNode("40"));
                    Element itemAAJ = document.createElement(BARRA_DATOS_CANTIDAD);
                    itemAAJ.appendChild(document.createTextNode("1"));
                    Element itemAAK = document.createElement(BARRA_DATOS_CORTE);
                    Element itemAAKA = document.createElement(BARRA_DATOS_CORTE_ANGIZQUIERDO);   

                    itemAAKA.appendChild(document.createTextNode("90.0"));
                    Element itemAAKB = document.createElement(BARRA_DATOS_CORTE_ANGDERECHO);              
                    itemAAKB.appendChild(document.createTextNode("90.0"));
                    Element itemAAKC = document.createElement(BARRA_DATOS_CORTE_LONGINFERIOR);
                    //itemAAKC.appendChild(document.createTextNode(listadoBarrasSistema.get(i).getLongitudInterior().toString()));
                    itemAAKC.appendChild(document.createTextNode(""));
                    Element itemAAKD = document.createElement(BARRA_DATOS_CORTE_LONGSUPERIOR);
                    itemAAKD.appendChild(document.createTextNode(sb2.getLongitud().toString()));
                    Element itemAAKE = document.createElement(BARRA_DATOS_CORTE_CODIGOUNICO);
                    String identificadorBarra = referencia + "-" + sb2.getLongitud().toString() + "-" + contBarras;
                    if (esResto){
                        identificadorBarra = identificadorBarra.replace(referencia,"RESTO");
                    }    
                      
                    itemAAKE.appendChild(document.createTextNode(identificadorBarra));
                    Element itemAAKF = document.createElement(BARRA_DATOS_CORTE_ETIQUETA);
                    if (esResto){
                        itemAAKF.appendChild(document.createTextNode("RESTO"));
                    } else {
                        itemAAKF.appendChild(document.createTextNode(referencia));
                    }
                    Element itemAAKG = document.createElement(BARRA_DATOS_CORTE_ETIQUETA);
                    if (esResto){
                        itemAAKG.appendChild(document.createTextNode("NO LACAR"));
                    } else {
                        itemAAKG.appendChild(document.createTextNode("PEDIDO LAC"));
                    }
                    itemAAKG.appendChild(document.createTextNode(referencia));
                    itemAAK.appendChild(itemAAKA); //Uniï¿½n principal de datos del corte
                    itemAAK.appendChild(itemAAKB);
                    itemAAK.appendChild(itemAAKC);
                    itemAAK.appendChild(itemAAKD);
                    itemAAK.appendChild(itemAAKE);
                    itemAAK.appendChild(itemAAKF);
                    itemAAK.appendChild(itemAAKG);
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
                raiz.appendChild(itemA);  
             
         }
         return referencia+"_LAC_X2_C.xml";
     }

     public static String crearFichero(String referencia, ArrayList<String> cadenas, String ral) throws IOException {
        
        File fichero = new File("/home/ftp/tronzadora/"+referencia+"_"+ral+"_lac.jpg");
        String formato = "jpg";
        String rutaImagenes = "/home/ftp/tronzadora/img/";
        Object colorRal = miColorRal(ral);
        Boolean existeFicheroColor = (colorRal instanceof String ? hayFicheroColor((String)colorRal) : Boolean.FALSE);
        ArrayList<AttributedString> lineasTexto = new ArrayList<>();
        int altoImagen = 600;
        int anchoImagen = 400;
        int tamanioFuente = 20;
        int desplazamientoLinea = 22;  // Inicializo a 25 un poco mas que el tamaño de letra por defecto.
        int alturaBloqueTexto = 0;
        String resultado = "No se ha generado el fichero de lacado para adjuntar a Navision.";
        
        // SEGUN NUMERO DE LINEAS; DIMENSIONAR EL TAMAÑO DE FUENTE
        // Tamaño de fuente
            if (cadenas.size()>10){  // Si es menor dejamos por defecto el 20 inicializado arriba)
                tamanioFuente = ((28 - (cadenas.size()) )>6 ? (28 - (cadenas.size())) : 12 );  // Por ejemplo si son 12 lineas, la letra se reduce de 20 por defecto a 28-12 = 16
            }
            desplazamientoLinea = (int) (tamanioFuente * 1.10); //Probamos con un 10% mas que el tamanio de letra que aparezca; 
            
            // Altura de bloque de texto y de imagen
            alturaBloqueTexto = ((cadenas.size()+3)*desplazamientoLinea) + 20;  // Cuento en el +3 y en el +20 las lineas vacias y el texto tamaño fijo del nombre del color el inicio y fin de los
            altoImagen = alturaBloqueTexto + 125;
            
            // Ancho del texto
            
        int cadenaMasLarga = 0;
        for (String s : cadenas){
            AttributedString as = new AttributedString(s);
            as.addAttribute(TextAttribute.FONT, new Font("TimesRoman", Font.BOLD, tamanioFuente));
            as.addAttribute(TextAttribute.FOREGROUND, Color.black);
            lineasTexto.add(as);
            if (s.length()>cadenaMasLarga){
                cadenaMasLarga = s.length();
            }
        }
        
        AttributedString asNoColor = null;
        String cadenaColor = "";
        if (colorRal != null) {
                if (colorRal instanceof Color){
                    cadenaColor = nombreRal;
                    asNoColor = new AttributedString(cadenaColor);
                    asNoColor.addAttribute(TextAttribute.FONT, new Font("TimesRoman", Font.BOLD, 20));
                    asNoColor.addAttribute(TextAttribute.FOREGROUND, Color.black);
                
                } else {
                    if (colorRal instanceof String){
                        if (existeFicheroColor){
                            cadenaColor = (String)colorRal;
                        } else {
                            cadenaColor = "NO SE ENCUENTRA ESE RAL. SELECCIONAR IMAGEN CLASICA";
                        }
                        asNoColor = new AttributedString(cadenaColor);
                        asNoColor.addAttribute(TextAttribute.FONT, new Font("TimesRoman", Font.BOLD, 20));
                        asNoColor.addAttribute(TextAttribute.FOREGROUND, Color.black);
                    }
            }
        }
        
        if (cadenaColor.length()>cadenaMasLarga){
            cadenaMasLarga = cadenaColor.length();
        }
            
        // Ancho del texto
            anchoImagen = (cadenaMasLarga*tamanioFuente) + 10;

            // Creamos la imagen para dibujar en ella.
            BufferedImage imagen = null;
            BufferedImage finalImg = null;
            
            if (existeFicheroColor){
                imagen = new BufferedImage(anchoImagen, alturaBloqueTexto, BufferedImage.TYPE_INT_RGB);
                finalImg = new BufferedImage(anchoImagen, altoImagen, BufferedImage.TYPE_INT_RGB);
                
            } else {
                imagen = new BufferedImage(anchoImagen, altoImagen, BufferedImage.TYPE_INT_RGB);
            
            }
            
            Graphics2D g = imagen.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, imagen.getWidth(), imagen.getHeight());
            int lineaEnY = desplazamientoLinea; // Desplazamiento inicial con el margen superior de la imagen
            for (AttributedString as1 : lineasTexto) {
                g.drawString(as1.getIterator(), 5, lineaEnY);
                lineaEnY += desplazamientoLinea;
            }
            lineaEnY += desplazamientoLinea; // Dejo algo mas de margen hasta el dibujo

            g.drawString(asNoColor.getIterator(), 5, lineaEnY);
            lineaEnY += desplazamientoLinea;
            
            if (colorRal!=null){
                if (colorRal instanceof Color){
                    g.setColor((Color)colorRal);
                    g.fillRect(5, lineaEnY, anchoImagen - 10, (altoImagen - 125));
                } else if (colorRal instanceof String){
                    // cargar el fichero de imagen e insertarlo.
                     if (existeFicheroColor){
                        BufferedImage imagenCargada =  new BufferedImage(anchoImagen, 125, BufferedImage.TYPE_INT_RGB);
                        imagenCargada = ImageIO.read(new File(rutaImagenes + (String)colorRal));
                        Graphics2D gFinal = finalImg.createGraphics();
                        gFinal.setColor(Color.WHITE);
                        gFinal.fillRect(0, 0, finalImg.getWidth(), finalImg.getHeight());
                        finalImg.createGraphics().drawImage(imagen, null, 0, 0);
                        finalImg.createGraphics().drawImage(imagenCargada, null, 0, alturaBloqueTexto);
                     }
                    
            }    
            
            g.dispose();
            
            try {
                if (existeFicheroColor){
                    ImageIO.write(finalImg, formato, fichero);
                    resultado = "Fichero para el pedido de lacado NAV generado (Fichero de imagen previa): " + referencia+"_"+ral+"_lac.jpg";
                } else {
                    ImageIO.write(imagen, formato, fichero);
                    resultado = "Fichero para el pedido de lacado NAV generado (RAL automatico): " + referencia+"_"+ral+"_lac.jpg";
                }    
            } catch (IOException e) {
                System.out.println("Error de escritura");
                
            }
        }
        return resultado;   
    }

    public Boolean enviaErrorAAdministradores(String detalleAsunto, String detalleCuerpo, Exception exception) throws EmailException{

        HtmlEmail email = inicializaMail("dpto.informatica@todocristal.eu", "Administrador");
        try{

            email.setSubject("APPFABRICA03 - ERROR " + detalleAsunto + " " + exception.getClass());

            email.setHtmlMsg("<html>En la aplicaciÃ³n de diseÃ±o ha ocurrido el siguiente error: <br>"
                            + "<b>" + detalleCuerpo + "</b> <br>"
                            + exception.toString() + "</html>");

            if(!bloquearEnvio){
                email.send();
            } 
        } catch (EmailException eEx){
            eEx.printStackTrace();
        }
        return false;
    }
    
    public Boolean notificarProyectoParaProcesar(String nombreProyecto) {
        HtmlEmail email = inicializaMail("agarcia@todocristal.eu", "Se genera fichero para procesar los ficheros TMP del proyecto");
        try {
            email.setSubject("APPFABRICA99 MOCHILIFICADOR 2.0 " + nombreProyecto);
            email.setHtmlMsg("<html>El proyecto: " + nombreProyecto + " se ha generado fichero para PROCESAR los ficheros TMP</html>");
            if (!bloquearEnvio) {
                email.send();
            }
        } catch (EmailException eEx) {
            eEx.printStackTrace();
        }
        return false;

    }
    
    public Boolean notificarProyectoMochilificado(String nombreproyecto, String rutaArchivo, String auxColor, Integer emisor) throws EmailException{
        
        HtmlEmail email = inicializaMail("dpto.informatica@todocristal.eu", "Seguimiento mochilificación");
        String emisorTexto;
        try{   
            if (emisor!=null){
                switch (emisor){
                    case 0:
                        emisorTexto = "DISEÑO";
                        break;
                    case 1:
                        emisorTexto = "EXCEL";
                        break;
                    case 2:
                        emisorTexto = "FORZADO POSTMAN";
                        break;
                    default:
                        emisorTexto = "SIN EMISOR REGISTRADO";
                        break;
                } 
            } else {
                emisorTexto = "SIN EMISOR REGISTRADO";        
            }
                    
            
            email.setSubject("APPFABRICA01 MOCHILIFICADOR 2.0 " + nombreproyecto + " - " + auxColor + " desde: " + emisorTexto);
            
            email.setHtmlMsg("<html>El proyecto: " + nombreproyecto +  " de color " + auxColor + " ha sido mochilificado por " + emisorTexto  + "</html>");
            
            email.attach(cargarArchivoAdjunto(nombreproyecto, rutaArchivo));
            
            if(!bloquearEnvio){
                email.send();
            } 
        } catch (EmailException eEx){
            eEx.printStackTrace();
        }
        return false;
    }
    
    public Boolean notificarLiberadasBarras(String nombreproyecto, String color) throws EmailException{
        
        HtmlEmail email = inicializaMail("dpto.informatica@todocristal.eu", "Seguimiento mochilificación");
        try{            
            email.setSubject("APPFABRICA04 LIBERADAS BARRAS 2.0 " + nombreproyecto + " - " + color);
            
            email.setHtmlMsg("<html>El proyecto: " + nombreproyecto + " ha liberado barras de color " + color + "</html>");
            
            if(!bloquearEnvio){
                email.send();
            } 
        } catch (EmailException eEx){
            eEx.printStackTrace();
        }
        return false;
    }
    
    public Boolean notificarAgregadosRestos(String nombreproyecto, String color, Boolean hayRestosAprovechables) throws EmailException{
        
        HtmlEmail email = inicializaMail("dpto.informatica@todocristal.eu", "Seguimiento mochilificación");
        try{            
            email.setSubject("APPFABRICA05 AGREGAR RESTOS 2.0 " + nombreproyecto + " - " + color);
            if (hayRestosAprovechables){
                email.setHtmlMsg("<html>El proyecto: " + nombreproyecto + " ha agregado restos de color " + color + "</html>");
            } else {
                email.setHtmlMsg("<html>El proyecto: " + nombreproyecto + " No ha agregado restos de color " + color + " ya que ninguno cummplia con las condiciones de medida minima aprovechable</html>");
            }    
            
            if(!bloquearEnvio){
                email.send();
            } 
        } catch (EmailException eEx){
            eEx.printStackTrace();
        }
        return false;
    }
    
    
    private EmailAttachment cargarArchivoAdjunto(String referencia, String direccionArchivo){
            
        EmailAttachment attachment = new EmailAttachment();
        

        //attachment.setPath("\\\\ALLGLASSSERVER\\producción\\pdfapp\\"+ nombreProyecto + "(" + version + ").pdf");
        //attachment.setPath("/home/comercial/presupuestos/" + SecurityUtils.userPrincipal().getName() + "/" + referencia + ".pdf");
        attachment.setPath(direccionArchivo);
        //attachment.setPath(referencia);
        attachment.setDisposition(EmailAttachment.ATTACHMENT);
        attachment.setDescription(referencia);
        attachment.setName(referencia + ".txt");        

        return attachment;
    }
    
    public Boolean getBloquearEnvio() {
        return bloquearEnvio;
    }

    public void setBloquearEnvio(Boolean bloquearEnvio) {
        this.bloquearEnvio = bloquearEnvio;
    }
    
    /*nuevoS GRUPOS*/
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
    
    private static Object miColorRal(String ral) {
        class miColor {

            String ral;
            String nombre;
            int r;
            int g;
            int b;

            public miColor(String ral, String nombre, int r, int g, int b) {
                this.ral = ral;
                this.nombre = nombre;
                this.r = r;
                this.g = g;
                this.b = b;
            }

            public String getRal() {
                return ral;
            }

            public void setRal(String ral) {
                this.ral = ral;
            }

            public String getNombre() {
                return nombre;
            }

            public void setNombre(String nombre) {
                this.nombre = nombre;
            }

            public int getR() {
                return r;
            }

            public void setR(int r) {
                this.r = r;
            }

            public int getG() {
                return g;
            }

            public void setG(int g) {
                this.g = g;
            }

            public int getB() {
                return b;
            }

            public void setB(int b) {
                this.b = b;
            }

        }
        ArrayList<miColor> misColores = new ArrayList<>();

        misColores.add(new miColor("1000", "1000 Beige verdoso", 190, 189, 127));
        misColores.add(new miColor("1001", "1001 Beige", 194, 176, 120));
        misColores.add(new miColor("1002", "1002 Amarillo arena", 198, 166, 100));
        misColores.add(new miColor("1003", "1003 Amarillo señales", 229, 190, 1));
        misColores.add(new miColor("1004", "1004 Amarillo oro", 205, 164, 52));
        misColores.add(new miColor("1005", "1005 Amarillo miel", 169, 131, 7));
        misColores.add(new miColor("1006", "1006 Amarillo maiz", 228, 160, 16));
        misColores.add(new miColor("1007", "1007 Amarillo narciso", 220, 156, 0));
        misColores.add(new miColor("1011", "1011 Beige pardo", 138, 102, 66));
        misColores.add(new miColor("1012", "1012 Amarillo limón", 199, 180, 70));
        misColores.add(new miColor("1013", "1013 Blanco perla", 234, 230, 202));
        misColores.add(new miColor("1014", "1014 Marfil", 225, 204, 79));
        misColores.add(new miColor("1015", "1015 Marfil claro", 230, 214, 144));
        misColores.add(new miColor("1016", "1016 Amarillo azufre", 237, 255, 33));
        misColores.add(new miColor("1017", "1017 Amarillo azafrán", 245, 208, 51));
        misColores.add(new miColor("1018", "1018 Amarillo de zinc", 248, 243, 53));
        misColores.add(new miColor("1019", "1019 Beige agrisado", 158, 151, 100));
        misColores.add(new miColor("1020", "1020 Amarillo oliva", 153, 153, 80));
        misColores.add(new miColor("1021", "1021 Amarillo colza", 243, 218, 11));
        misColores.add(new miColor("1023", "1023 Amarillo tráfico", 250, 210, 1));
        misColores.add(new miColor("1024", "1024 Amarillo ocre", 174, 160, 75));
        misColores.add(new miColor("1026", "1026 Amarillo brillante", 255, 255, 0));
        misColores.add(new miColor("1027", "1027 Amarillo curry", 157, 145, 1));
        misColores.add(new miColor("1028", "1028 Amarillo melón", 244, 169, 0));
        misColores.add(new miColor("1032", "1032 Amarillo retama", 214, 174, 1));
        misColores.add(new miColor("1033", "1033 Amarillo dalia", 243, 165, 5));
        misColores.add(new miColor("1034", "1034 Amarillo pastel", 239, 169, 74));
        misColores.add(new miColor("1035", "1035 Beige perlado", 106, 93, 77));
        misColores.add(new miColor("1036", "1036 Oro perlado", 112, 83, 53));
        misColores.add(new miColor("1037", "1037 Amarillo sol", 243, 159, 24));
        misColores.add(new miColor("2000", "2000 Amarillo naranja", 237, 118, 14));
        misColores.add(new miColor("2001", "2001 Rojo anaranjado", 201, 60, 32));
        misColores.add(new miColor("2002", "2002 Naranja sanguineo", 203, 40, 33));
        misColores.add(new miColor("2003", "2003 Naranja pálido", 255, 117, 20));
        misColores.add(new miColor("2004", "2004 Naranja puro", 244, 70, 17));
        misColores.add(new miColor("2005", "2005 Naranja brillante", 255, 35, 1));
        misColores.add(new miColor("2007", "2007 Naranja claro brillante", 255, 164, 32));
        misColores.add(new miColor("2008", "2008 Rojo claro anaranjado", 247, 94, 37));
        misColores.add(new miColor("2009", "2009 Naranja tráfico", 245, 64, 33));
        misColores.add(new miColor("2010", "2010 Naranja señales", 216, 75, 32));
        misColores.add(new miColor("2011", "2011 Naranja intenso", 236, 124, 38));
        misColores.add(new miColor("2012", "2012 Naranja salmón", 235, 106, 14));
        misColores.add(new miColor("2013", "2013 Naranja perlado", 195, 88, 49));
        misColores.add(new miColor("3000", "3000 Rojo vivo", 175, 43, 30));
        misColores.add(new miColor("3001", "3001 Rojo señales", 165, 32, 25));
        misColores.add(new miColor("3002", "3002 Rojo carmin", 162, 35, 29));
        misColores.add(new miColor("3003", "3003 Rojo rubí", 155, 17, 30));
        misColores.add(new miColor("3004", "3004 Rojo purpura", 117, 21, 30));
        misColores.add(new miColor("3005", "3005 Rojo vino", 94, 33, 41));
        misColores.add(new miColor("3007", "3007 Rojo negruzco", 65, 34, 39));
        misColores.add(new miColor("3009", "3009 Rojo óxido", 100, 36, 36));
        misColores.add(new miColor("3011", "3011 Rojo pardo", 120, 31, 25));
        misColores.add(new miColor("3012", "3012 Rojo beige", 193, 135, 107));
        misColores.add(new miColor("3013", "3013 Rojo tomate", 161, 35, 18));
        misColores.add(new miColor("3014", "3014 Rojo viejo", 211, 110, 112));
        misColores.add(new miColor("3015", "3015 Rosa claro", 234, 137, 154));
        misColores.add(new miColor("3016", "3016 Rojo coral", 179, 40, 33));
        misColores.add(new miColor("3017", "3017 Rosa", 230, 50, 68));
        misColores.add(new miColor("3018", "3018 Rojo fresa", 213, 48, 50));
        misColores.add(new miColor("3020", "3020 Rojo tráfico", 204, 6, 5));
        misColores.add(new miColor("3022", "3022 Rojo salmón", 217, 80, 48));
        misColores.add(new miColor("3024", "3024 Rojo brillante", 248, 0, 0));
        misColores.add(new miColor("3026", "3026 Rojo claro brillante", 254, 0, 0));
        misColores.add(new miColor("3027", "3027 Rojo frambuesa", 197, 29, 52));
        misColores.add(new miColor("3028", "3028 Rojo puro", 203, 50, 52));
        misColores.add(new miColor("3031", "3031 Rojo oriente", 179, 36, 40));
        misColores.add(new miColor("3032", "3032 Rojo rubí perlado", 114, 20, 34));
        misColores.add(new miColor("3033", "3033 Rosa perlado", 180, 76, 67));
        misColores.add(new miColor("4001", "4001 Rojo lila", 109, 63, 91));
        misColores.add(new miColor("4002", "4002 Rojo violeta", 146, 43, 62));
        misColores.add(new miColor("4003", "4003 Violeta érica", 222, 76, 138));
        misColores.add(new miColor("4004", "4004 Burdeos", 110, 28, 52));
        misColores.add(new miColor("4005", "4005 Lila azulado", 108, 70, 117));
        misColores.add(new miColor("4006", "4006 Púrpurá tráfico", 160, 52, 114));
        misColores.add(new miColor("4007", "4007 Violeta púrpura", 74, 25, 44));
        misColores.add(new miColor("4008", "4008 Violeta señales", 144, 70, 132));
        misColores.add(new miColor("4009", "4009 Violeta pastel", 164, 125, 144));
        misColores.add(new miColor("4010", "4010 Magenta tele", 215, 45, 109));
        misColores.add(new miColor("4011", "4011 Violeta perlado", 134, 115, 161));
        misColores.add(new miColor("4012", "4012 Morado perlado", 108, 104, 129));
        misColores.add(new miColor("5000", "5000 Azul violeta", 42, 46, 75));
        misColores.add(new miColor("5001", "5001 Azul verdoso", 31, 52, 56));
        misColores.add(new miColor("5002", "5002 Azul ultramar", 32, 33, 79));
        misColores.add(new miColor("5003", "5003 Azul zafiro", 29, 30, 51));
        misColores.add(new miColor("5004", "5004 Azul negruzco", 24, 23, 28));
        misColores.add(new miColor("5005", "5005 Azul señales", 30, 45, 110));
        misColores.add(new miColor("5007", "5007 Azul brillante", 62, 95, 138));
        misColores.add(new miColor("5008", "5008 Azul grisáceo", 38, 37, 45));
        misColores.add(new miColor("5009", "5009 Azul azur", 2, 86, 105));
        misColores.add(new miColor("5010", "5010 Azul genciana", 14, 41, 75));
        misColores.add(new miColor("5011", "5011 Azul acero", 35, 26, 36));
        misColores.add(new miColor("5012", "5012 Azul luminoso", 59, 131, 189));
        misColores.add(new miColor("5013", "5013 Azul cobalto", 37, 41, 74));
        misColores.add(new miColor("5014", "5014 Azul olombino", 96, 111, 140));
        misColores.add(new miColor("5015", "5015 Azul celeste", 34, 113, 179));
        misColores.add(new miColor("5017", "5017 Azul tráfico", 6, 57, 113));
        misColores.add(new miColor("5018", "5018 Azul turquesa", 63, 136, 143));
        misColores.add(new miColor("5019", "5019 Azul capri", 27, 85, 131));
        misColores.add(new miColor("5020", "5020 Azul oceano", 29, 51, 74));
        misColores.add(new miColor("5021", "5021 Azul agua", 37, 109, 123));
        misColores.add(new miColor("5022", "5022 Azul noche", 37, 40, 80));
        misColores.add(new miColor("5023", "5023 Azul lejanía", 73, 103, 141));
        misColores.add(new miColor("5024", "5024 Azul pastel", 93, 155, 155));
        misColores.add(new miColor("5025", "5025 Gencian perlado", 42, 100, 120));
        misColores.add(new miColor("5026", "5026 Azul noche perlado", 16, 44, 84));
        misColores.add(new miColor("6000", "6000 Verde patina", 49, 102, 80));
        misColores.add(new miColor("6001", "6001 Verde esmeralda", 40, 114, 51));
        misColores.add(new miColor("6002", "6002 Verde hoja", 45, 87, 44));
        misColores.add(new miColor("6003", "6003 Verde oliva", 66, 70, 50));
        misColores.add(new miColor("6004", "6004 Verde azulado", 31, 58, 61));
        misColores.add(new miColor("6005", "6005 Verde musgo", 47, 69, 56));
        misColores.add(new miColor("6006", "6006 Oliva grisáceo", 62, 59, 50));
        misColores.add(new miColor("6007", "6007 Verde botella", 52, 59, 41));
        misColores.add(new miColor("6008", "6008 Verde parduzco", 57, 53, 42));
        misColores.add(new miColor("6009", "6009 Verde abeto", 49, 55, 43));
        misColores.add(new miColor("6010", "6010 Verde hierba", 53, 104, 45));
        misColores.add(new miColor("6011", "6011 Verde reseda", 88, 114, 70));
        misColores.add(new miColor("6012", "6012 Verde negruzco", 52, 62, 64));
        misColores.add(new miColor("6013", "6013 Verde caña", 108, 113, 86));
        misColores.add(new miColor("6014", "6014 Amarillo oliva", 71, 64, 46));
        misColores.add(new miColor("6015", "6015 Oliva negruzco", 59, 60, 54));
        misColores.add(new miColor("6016", "6016 Verde turquesa", 30, 89, 69));
        misColores.add(new miColor("6017", "6017 Verde mayo", 76, 145, 65));
        misColores.add(new miColor("6018", "6018 Verde amarillento", 87, 166, 57));
        misColores.add(new miColor("6019", "6019 Verde lanquecino", 189, 236, 182));
        misColores.add(new miColor("6020", "6020 Verde cromo", 46, 58, 35));
        misColores.add(new miColor("6021", "6021 Verde pálido", 137, 172, 118));
        misColores.add(new miColor("6022", "6022 Oliva parduzco", 37, 34, 27));
        misColores.add(new miColor("6024", "6024 Verde tráfico", 48, 132, 70));
        misColores.add(new miColor("6025", "6025 Verde helecho", 61, 100, 45));
        misColores.add(new miColor("6026", "6026 Verde opalo", 1, 93, 82));
        misColores.add(new miColor("6027", "6027 Verde luminoso", 132, 195, 190));
        misColores.add(new miColor("6028", "6028 Verde pino", 44, 85, 69));
        misColores.add(new miColor("6029", "6029 Verde menta", 32, 96, 61));
        misColores.add(new miColor("6032", "6032 Verde señales", 49, 127, 67));
        misColores.add(new miColor("6033", "6033 Turquesa menta", 73, 126, 118));
        misColores.add(new miColor("6034", "6034 Turquesa pastel", 127, 181, 181));
        misColores.add(new miColor("6035", "6035 Verde perlado", 28, 84, 45));
        misColores.add(new miColor("6036", "6036 Verde ópalo perlado", 22, 53, 55));
        misColores.add(new miColor("6037", "6037 Verde puro", 0, 143, 57));
        misColores.add(new miColor("6038", "6038 Verde brillante", 0, 187, 45));
        misColores.add(new miColor("7000", "7000 Gris ardilla", 120, 133, 139));
        misColores.add(new miColor("7001", "7001 Gris plata", 138, 149, 151));
        misColores.add(new miColor("7002", "7002 Gris oliva", 126, 123, 82));
        misColores.add(new miColor("7003", "7003 Gris musgo", 108, 112, 89));
        misColores.add(new miColor("7004", "7004 Gris señales", 150, 153, 146));
        misColores.add(new miColor("7005", "7005 Gris ratón", 100, 107, 99));
        misColores.add(new miColor("7006", "7006 Gris beige", 109, 101, 82));
        misColores.add(new miColor("7008", "7008 Gris caqui", 106, 95, 49));
        misColores.add(new miColor("7009", "7009 Gris verdoso", 77, 86, 69));
        misColores.add(new miColor("7010", "7010 Gris lona", 76, 81, 74));
        misColores.add(new miColor("7011", "7011 Gris hierro", 67, 75, 77));
        misColores.add(new miColor("7012", "7012 Gris basalto", 78, 87, 84));
        misColores.add(new miColor("7013", "7013 Gris parduzco", 70, 69, 49));
        misColores.add(new miColor("7015", "7015 Gris pizarra", 67, 71, 80));
        misColores.add(new miColor("7016", "7016 Gris antracita", 41, 49, 51));
        misColores.add(new miColor("7021", "7021 Gris negruzco", 35, 40, 43));
        misColores.add(new miColor("7022", "7022 Gris sombra", 51, 47, 44));
        misColores.add(new miColor("7023", "7023 Gris hormigón", 104, 108, 94));
        misColores.add(new miColor("7024", "7024 Gris grafita", 71, 74, 81));
        misColores.add(new miColor("7026", "7026 Gris granito", 47, 53, 59));
        misColores.add(new miColor("7030", "7030 Gris piedra", 139, 140, 122));
        misColores.add(new miColor("7031", "7031 Gris azulado", 71, 75, 78));
        misColores.add(new miColor("7032", "7032 Gris guijarro", 184, 183, 153));
        misColores.add(new miColor("7033", "7033 Gris cemento", 125, 132, 113));
        misColores.add(new miColor("7034", "7034 Gris amarillento", 143, 139, 102));
        misColores.add(new miColor("7035", "7035 Gris luminoso", 203, 208, 204));
        misColores.add(new miColor("7036", "7036 Gris platino", 127, 118, 121));
        misColores.add(new miColor("7037", "7037 Gris polvo", 125, 127, 120));
        misColores.add(new miColor("7038", "7038 Gris ágata", 195, 195, 195));
        misColores.add(new miColor("7039", "7039 Gris cuarzo", 108, 105, 96));
        misColores.add(new miColor("7040", "7040 Gris ventana", 157, 161, 170));
        misColores.add(new miColor("7042", "7042 Gris tráfico A", 141, 148, 141));
        misColores.add(new miColor("7043", "7043 Gris tráfico B", 78, 84, 82));
        misColores.add(new miColor("7044", "7044 Gris seda", 202, 196, 176));
        misColores.add(new miColor("7045", "7045 Gris tele 1", 144, 144, 144));
        misColores.add(new miColor("7046", "7046 Gris tele 2", 130, 137, 143));
        misColores.add(new miColor("7047", "7047 Gris tele 4", 208, 208, 208));
        misColores.add(new miColor("7048", "7048 Gris musgo perlado", 137, 129, 118));
        misColores.add(new miColor("8000", "8000 Pardo verdoso", 130, 108, 52));
        misColores.add(new miColor("8001", "8001 Pardo ocre", 149, 95, 32));
        misColores.add(new miColor("8002", "8002 Marrón señales", 108, 59, 42));
        misColores.add(new miColor("8003", "8003 Pardo arcilla", 115, 66, 34));
        misColores.add(new miColor("8004", "8004 Pardo cobre", 142, 64, 42));
        misColores.add(new miColor("8007", "8007 Pardo corzo", 89, 53, 31));
        misColores.add(new miColor("8008", "8008 Pardo oliva", 111, 79, 40));
        misColores.add(new miColor("8011", "8011 Pardo nuez", 91, 58, 41));
        misColores.add(new miColor("8012", "8012 Pardo rojo", 89, 35, 33));
        misColores.add(new miColor("8014", "8014 Sepia", 56, 44, 30));
        misColores.add(new miColor("8015", "8015 Castaño", 99, 58, 52));
        misColores.add(new miColor("8016", "8016 Caoba", 76, 47, 39));
        misColores.add(new miColor("8017", "8017 Chocolate", 69, 50, 46));
        misColores.add(new miColor("8019", "8019 Pardo grisáceo", 64, 58, 58));
        misColores.add(new miColor("8022", "8022 Pardo negruzco", 33, 33, 33));
        misColores.add(new miColor("8023", "8023 Pardo anaranjado", 166, 94, 46));
        misColores.add(new miColor("8024", "8024 Pardo beige", 121, 85, 61));
        misColores.add(new miColor("8025", "8025 Pardo pálido", 117, 92, 72));
        misColores.add(new miColor("8028", "8028 Marrón tierra", 78, 59, 49));
        misColores.add(new miColor("8029", "8029 Cobre perlado", 118, 60, 40));
        misColores.add(new miColor("9001", "9001 Blanco crema", 250, 244, 227));
        misColores.add(new miColor("9002", "9002 Blanco grisáceo", 231, 235, 218));
        misColores.add(new miColor("9003", "9003 Blanco señales", 244, 244, 244));
        misColores.add(new miColor("9004", "9004 Negro señales", 40, 40, 40));
        misColores.add(new miColor("9005", "9005 Negro intenso", 10, 10, 13));
        misColores.add(new miColor("9006", "9006 Aluminio blanco", 165, 165, 165));
        misColores.add(new miColor("9007", "9007 Aluminio gris", 143, 143, 143));
        misColores.add(new miColor("9010", "9010 Blanco puro", 255, 255, 255));
        misColores.add(new miColor("9011", "9011 Negro grafito", 28, 28, 28));
        misColores.add(new miColor("9016", "9016 Blanco tráfico", 246, 246, 246));
        misColores.add(new miColor("9017", "9017 Negro tráfico", 30, 30, 30));
        misColores.add(new miColor("9018", "9018 Blanco papiro", 207, 211, 205));
        misColores.add(new miColor("9022", "9022 Gris claro perlado", 156, 156, 156));
        misColores.add(new miColor("9023", "9023 Gris oscuro perlado", 130, 130, 130));

        misColores.add(new miColor("MA0001", "MA0001 WENGE", 999, 999, 999));
        misColores.add(new miColor("MA0002", "MA0002 ALISO", 999, 999, 999));
        misColores.add(new miColor("MA0003", "MA0003 CEREZO G7", 999, 999, 999));
        misColores.add(new miColor("MA0004", "MA0004 NOGAL OSCURO", 999, 999, 999));
        misColores.add(new miColor("MA0005", "MA0005 ROBLE OSCURO", 999, 999, 999));
        misColores.add(new miColor("MA0006", "MA0006 ROBLE RUSTICO", 999, 999, 999));
        misColores.add(new miColor("MA0007", "MA0007 TEKA", 999, 999, 999));
        misColores.add(new miColor("MA0011", "MA0011 CEREZO CON CATEDRALES", 999, 999, 999));
        misColores.add(new miColor("MA0012", "MA0012 PINO NUDO", 999, 999, 999));
        misColores.add(new miColor("MA0013", "MA0013 ACACIA A7", 999, 999, 999));
        misColores.add(new miColor("MA0014", "MA0014 CEDRO", 999, 999, 999));
        misColores.add(new miColor("MA0015", "MA0015 CASTAÑO", 999, 999, 999));
        misColores.add(new miColor("MA0019", "MA0019 EMBERO", 999, 999, 999));
        misColores.add(new miColor("MA0020", "MA0020 NOGAL ANDALUZ", 999, 999, 999));
        misColores.add(new miColor("MA0021", "MA0021 PINO", 999, 999, 999));
        misColores.add(new miColor("MA0022", "MA0022 HAYA", 999, 999, 999));
        misColores.add(new miColor("MA0024", "MA0024 PINO MEDIO", 999, 999, 999));
        misColores.add(new miColor("MA0027", "MA0027 SAPELLY CAOBA", 999, 999, 999));
        misColores.add(new miColor("MA0028", "MA0028 ROBLE GOLDEN", 999, 999, 999));
        misColores.add(new miColor("MA0030", "MA0030 NOGAL EUROPEO", 999, 999, 999));
        misColores.add(new miColor("MA0031", "MA0031 PINO ENVEJECIDO", 999, 999, 999));
        misColores.add(new miColor("MA0032", "MA0032 DOUGLAS EUROPEO P8", 999, 999, 999));
        misColores.add(new miColor("MA0036", "MA0036 AFRIC", 999, 999, 999));
        misColores.add(new miColor("MA0039", "MA0039 ROBLE E7", 999, 999, 999));
        misColores.add(new miColor("MA0041", "MA0041 NOGAL B7", 999, 999, 999));
        misColores.add(new miColor("MA0042", "MA0042 CEREZO DORADO", 999, 999, 999));
        misColores.add(new miColor("MA0044", "MA0044 SAPELLY MARRON", 999, 999, 999));
        misColores.add(new miColor("MA0050", "MA0050 ROBLE ASSI", 999, 999, 999));
        
        misColores.add(new miColor("AN0001", "AN0001 BRONCE", 999, 999, 999));
        misColores.add(new miColor("AN0002", "AN0002 PLATA", 999, 999, 999));
        misColores.add(new miColor("AN0003", "AN0003 ORO", 999, 999, 999));
        misColores.add(new miColor("AN0004", "AN0004 ANODIZADO NEGRO", 999, 999, 999));
        
        misColores.add(new miColor("ES0002", "ES0002 OXIRON NEGRO", 999, 999, 999));
        misColores.add(new miColor("ES0003", "ES0003 GRAFITO NEGRO", 999, 999, 999));
        misColores.add(new miColor("ES0006", "ES0006 NEGRO MATE", 10, 10, 13)); // MISMO COLOR QUE EL 9005
        misColores.add(new miColor("ES0008", "ES0008 AKZONOBEL BRONZE 2525 YW283F", 999, 999, 999));
        misColores.add(new miColor("ES0009", "ES0009 AKZONOBEL MARS 2525 SABLÉ YX355F", 999, 999, 999));
        
        

        for (miColor mC : misColores) { // Si lo encuentra devuelve un Color
            if (mC.getRal().startsWith(ral)) {
                nombreRal = mC.getNombre();
                if (nombreRal.endsWith("T")) {
                    nombreRal = nombreRal + " TEXTURADO";
                } else if (nombreRal.endsWith("M")) {
                    nombreRal = nombreRal + " MATE";
                }
                if (mC.getR() == 999) {
                    return new String(mC.getNombre() + ".jpg");
                }
                return new Color(mC.getR(), mC.getG(), mC.getB());

            }
        }

        return ("NO EXISTE");
    }

private static Boolean hayFicheroColor (String nombreFichero){
                final String nombreParaFiltro = nombreFichero;
                String ruta = "/home/ftp/tronzadora/img/";
                File carpeta = new File(ruta);
                FilenameFilter filtroNombre = new FilenameFilter(){
                    @Override
                    public boolean accept (File dir, String name){
                        return name.startsWith(nombreParaFiltro);
                    }
                };
                File[] files  = carpeta.listFiles(filtroNombre);
                if (files!=null && files.length>0){
                    return true;
                }
     return false;
}
      
}
