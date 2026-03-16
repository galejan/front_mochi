/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilidades;

import com.todocristal.fabrica.webservice.auxiliar.AuxLogger;
import com.todocristal.fabrica.webservice.model.Barras;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 *
 * @author rafael
 */
public class FileUtil {
    //TODO: DESARROLLAR LA CLASE PARA UTILIZARLA CON DIFERENTES FUNCIONALIDADES
    //1. GUARDAR LOG DE CADA UNO DE LOS PROYECTOS, carga, mochilificado, agregado de barras, limpiado, etc..
    private Calendar fecha = new GregorianCalendar();  // Usamos el Calendar para gestionar las carpetas
    private Date fechaLog = new Date(); // Usamos la fecha estandar para el log en si
    
    final String TAG_RUTA_LOGS = "/home/logs/";
    private final static String OUTPUT_FOLDER_TRONZADORA ="/home/ftp/tronzadora/";
    private MailUtil mail = new MailUtil();
    
    public void registraFicheroProcesar(String referenciaProyecto)throws IOException{
        BufferedWriter bw = null;
        
        String ruta = OUTPUT_FOLDER_TRONZADORA+referenciaProyecto+"_PROCESAR.txt";

        File rutaDirectorio = new File(TAG_RUTA_LOGS);
        File archivo = new File(ruta);

        try{            

            if (!rutaDirectorio.exists()){
                rutaDirectorio.mkdirs();
            }

            if(archivo.exists()) {
                archivo.delete();
                bw = new BufferedWriter(new FileWriter(archivo,true));                    
            } else {
                bw = new BufferedWriter(new FileWriter(archivo,true));                 
            }
            mail.notificarProyectoParaProcesar(referenciaProyecto);
        }catch (Exception e){
            e.printStackTrace();
            bw.close();
        }finally{
            if(bw!=null)
                bw.close();
        }
    }
    
    public void registrarLogProyecto(String referenciaProyecto, List<AuxLogger> logOperaciones, String auxColor, String auxLogResultado, Integer emisor) throws IOException{           
        BufferedWriter bw = null;
        
        String ruta = TAG_RUTA_LOGS+Integer.toString(fecha.get(Calendar.YEAR))+Integer.toString(fecha.get(Calendar.MONTH)+1) + "/log"+referenciaProyecto+".txt";

        File rutaDirectorio = new File(TAG_RUTA_LOGS+Integer.toString(fecha.get(Calendar.YEAR))+Integer.toString(fecha.get(Calendar.MONTH)+1));
        File archivo = new File(ruta);

        try{            

            if (!rutaDirectorio.exists()){
                rutaDirectorio.mkdirs();
            }

            if(archivo.exists()) {
                archivo.delete();
                bw = new BufferedWriter(new FileWriter(archivo,true));                    
            } else {
                bw = new BufferedWriter(new FileWriter(archivo,true));                 
            }
            bw.newLine();
            //bw.write(fecha.getTime().getTime()+ ";" + logOperaciones.toString() + "\n");
            bw.write(fechaLog.toString() + "\n" 
                    + "Texto1:Color:Longitud_barra_stock:Tipo_perfil:Texto2:Longitud_barra:Texto3:Num_barras_mochi:Texto4:Merma_media:Texto5:Resto \n"+ logOperaciones.toString() + "\n" + auxLogResultado + "\n");
            bw.close();
            mail.notificarProyectoMochilificado(referenciaProyecto, ruta, auxColor, emisor);
        }catch (Exception e){
            e.printStackTrace();
            bw.close();
        }finally{
            if(bw!=null)
                bw.close();
        }
    }
    
    public void registrarLogMovimientos(String auxColor, String auxLogRestos) throws IOException{
        BufferedWriter bw = null;
        String ruta = TAG_RUTA_LOGS + auxColor +".txt";

        File rutaDirectorio = new File(TAG_RUTA_LOGS);
        File archivo = new File(ruta);
        
        try{            

            if (!rutaDirectorio.exists()){
                rutaDirectorio.mkdirs();
            }

            if(archivo.exists()) {
                bw = new BufferedWriter(new FileWriter(archivo,true));                    
            } else {
                bw = new BufferedWriter(new FileWriter(archivo,true));
                bw.write("Fecha;Referencia;Sistema;Perfil;Color;Grupo;Proceso;Movimiento\n");
            }
            //bw.newLine();
            bw.write(auxLogRestos.replaceAll("\n", System.getProperty("line.separator")));
            //bw.write(auxLogRestos);
            bw.close();
            
        }catch (Exception e){
            e.printStackTrace();
            bw.close();
        }finally{
            if(bw!=null)
                bw.close();
        }
    }
    
    public void logMochilificacionesAtxt (String referenciaProyecto, String logMochilificaciones) throws IOException{           
        BufferedWriter bw = null;
        
        String ruta = TAG_RUTA_LOGS+referenciaProyecto+"_moch.txt";

        File rutaDirectorio = new File(TAG_RUTA_LOGS);
        File archivo = new File(ruta);

        try{            

            if (!rutaDirectorio.exists()){
                rutaDirectorio.mkdirs();
            }

            if(archivo.exists()) {
                //archivo.delete();
                bw = new BufferedWriter(new FileWriter(archivo,true));                    
            } else {
                bw = new BufferedWriter(new FileWriter(archivo,true));                 
            }
            bw.write("Barras;Perfil;Longitud;Merma;MermaMedia;Resto;SotckBarra;ObligadaStock\n");
            bw.write(logMochilificaciones);
            bw.close();
            
        }catch (Exception e){
            e.printStackTrace();
            bw.close();
        }finally{
            if(bw!=null)
                bw.close();
        }
    }
    
    public void logBarrasMochilificadasAtxt (String referenciaProyecto, List<Barras> barras, Boolean anadeAlFichero) throws IOException{           
        BufferedWriter bw = null;
        String ruta = TAG_RUTA_LOGS+referenciaProyecto+"_barras.txt";
        File rutaDirectorio = new File(TAG_RUTA_LOGS);
        File archivo = new File(ruta);
        try{            
            if (barras!=null && !barras.isEmpty()){
                if (!rutaDirectorio.exists()){
                    rutaDirectorio.mkdirs();
                }
                if(archivo.exists()) {
                    //archivo.delete();
                    bw = new BufferedWriter(new FileWriter(archivo,anadeAlFichero));                    
                } else {
                    bw = new BufferedWriter(new FileWriter(archivo,true));                 
                }
                for (Barras bar : barras){
                    bw.write(bar.toString());
                }
                bw.close();
            }
        }catch (Exception e){
            e.printStackTrace();
            bw.close();
        }finally{
            if(bw!=null)
                bw.close();
        }
    }
    
    public ArrayList<Barras> barrasDesdeFicheroTxt(String referenciaProyecto) throws IOException{
        BufferedReader br = null;
        String ruta = TAG_RUTA_LOGS+referenciaProyecto+"_barras.txt";
        File rutaDirectorio = new File(TAG_RUTA_LOGS);
        File archivo = new File(ruta);
        ArrayList<Barras> barras = new ArrayList<>();
        try{            
                if(archivo.exists()) {
                    //archivo.delete();
                    br = new BufferedReader(new FileReader(archivo));                    
                } else {
                    return barras; // Ira vacio.                 
                }
                String line = null;
                while((line = br.readLine()) != null) {
                    barras.add(new Barras().barraDesdeTxt(line));
                }
                
                br.close();
           
        }catch (Exception e){
            e.printStackTrace();
            br.close();
        }finally{
            if(br!=null)
                br.close();
        }
        
        return barras;
    }
}
