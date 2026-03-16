package com.todocristal.fabrica.webservice.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import com.todocristal.fabrica.webservice.model.Barras;
import com.todocristal.fabrica.webservice.services.BarraServices;
import com.todocristal.fabrica.webservice.services.LogBarraServices;
import com.todocristal.fabrica.webservice.services.ProyectoServices;
import com.todocristal.fabrica.webservice.services.StockBarrasServices;
import java.util.ArrayList;
//import javafx.util.converter.BigIntegerStringConverter;
import javax.transaction.Transactional;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import utilidades.Mochilificador;
//AYUDA
//https://docs.jboss.org/hibernate/orm/3.3/reference/en/html/querycriteria.html

@Repository
@Transactional  
public class BarraDaoImpl implements BarraDao {
    
    @Autowired
    SessionFactory sessionFactory;
    
    @Autowired
    StockBarrasServices stockBarrasService;
    
    @Autowired
    BarraServices barraServices;
    
    @Autowired
    LogBarraServices logBarraServices;  
    
    @Autowired
    ProyectoServices proyectoServices;    

    Session session = null;
    Transaction tx = null;

    @Override
    public boolean addBarra(Barras barra) throws Exception {

            session = sessionFactory.openSession();
            tx = session.beginTransaction();
            session.saveOrUpdate(barra);
            tx.commit();
            session.close();

            return false;
    }

    @Override
    public Barras getBarraById(long id) throws Exception {
            session = sessionFactory.openSession();	
            tx = session.beginTransaction();
            Barras barra = (Barras) session.get(Barras.class, id);
            tx.commit();
            session.close();
            return barra;
    }
    /*
        --------   OBTENEMOS INFORMACIÓN SEGÚN LA REFERENCIA, ASÍ PODEMOS OBTENER EL ID PARA TRATAR EL PROYECTO Y LAS TAREAS.
        EN LA SINCRONIZACIÓN.
    */
    @Override
    public List<Barras> getBarrasByProyecto(String referencia) throws Exception{
            session = sessionFactory.openSession();	
            tx = session.beginTransaction();

            Criteria crit = session.createCriteria(Barras.class)
                .createAlias("proyecto", "p")
                .add(Restrictions.like("p.referencia", referencia));
            
            List<Barras> barras = crit.list();

            tx.commit();
            session.close();
            return barras;

    }

    
    @Override
    public List<Barras> getBarras() throws Exception {
            session = sessionFactory.openSession();
            tx = session.beginTransaction();
            List<Barras> barra = session.createCriteria(Barras.class).list();                
            tx.commit();
            session.close();

           // List<Barras> proyectos = session.createCriteria(Barras.class).list();                
            return barra;
    }

    @Override
    public boolean deleteBarra(long id) throws Exception {
            session = sessionFactory.openSession();
            Object o = session.load(Barras.class, id);
            tx = session.getTransaction();
            session.beginTransaction();
            session.delete(o);
            tx.commit();
            return false;
    }

    @Override
    public boolean updateBarras(List<Barras> barras) throws Exception {
        String logUpdate = "No ha llegado a procesar barra";
        try {
            session = sessionFactory.openSession();
            tx = session.beginTransaction();

            for (Barras b : barras) {
                logUpdate = b.getIdentificadorUnicoBarra();
                session.update(b);
            }
            logUpdate += " ES ULTIMA BARRA";
            tx.commit();
            session.close();
        } catch (HibernateException ex) {
            System.out.println("Ultima barra: " + logUpdate + ". Error hibernate:" + ex.getMessage());
        }

        return false;
    }
    
    @Override
    public boolean borraBarras(List<Barras> barras) throws Exception{
            session = sessionFactory.openSession();
            tx = session.beginTransaction();

            for(Barras b : barras){
                session.delete(b);
            }

            tx.commit();
            session.close();

            return false;
    } 
    
    /*@Override
    public boolean updateBarraPorReferencia(Barras proyecto) throws Exception{            
        session = sessionFactory.openSession();
        tx = session.beginTransaction();            
        session.update(proyecto);
        tx.commit();
        session.close();

        return false;
    }*/

    // DEVOLVEMOS TODAS LAS BARRAS QUE NO TIENEN ASOCIADA MERMA CONSUMIDA
    @Override
    public List<Barras> getBarrasEnCurso() throws Exception{
        List<Barras> barrasEnCurso=null;
        session = sessionFactory.openSession();
        tx = session.beginTransaction();

        /*String strQuery = "SELECT distinct barras.id from barras "
                + "left join mermas on mermas.barra_id = barras.id "
                + "right join eventos on eventos.actividad_id = actividades.id "
                + "where eventos.accion_id != 4";

        SQLQuery query = session.createSQLQuery(strQuery);
        List<BigInteger> idBarrasEnCurso = (List<BigInteger>)query.list();
        System.out.println(idBarrasEnCurso.toString());
        if(idBarrasEnCurso.size()>0)
            barrasEnCurso = new ArrayList<Barras>();
        for(BigInteger i : idBarrasEnCurso){
            Barras p = (Barras) session.createCriteria(Barras.class)
                    .add(Restrictions.eq("id",  i.longValue())).uniqueResult();
            barrasEnCurso.add(p);
        }
        */
        tx.commit();
        session.close();

        return barrasEnCurso;
    }

    @Override
    public boolean deleteBarrasByProyecto(String referenciaProyecto) {
        
        session = sessionFactory.openSession();
        Criteria crit = session.createCriteria(Barras.class)
            .createAlias("proyecto", "p")
            .add( Restrictions.like("p.referencia", referenciaProyecto));
            
            List<Barras> listaBarras = crit.list();
            for(Barras b : listaBarras){
                System.out.println(b.getLongitud());
                session.delete(b);
            }
            tx = session.getTransaction();
            session.beginTransaction();
            
            tx.commit();
            return false;
    }

    @Override
    public String mochilificarBarrasByProyecto(String referenciaProyecto) throws Exception{
        List<Barras> listadoBarrasProyecto ; 
        List<List<Barras>> barrasNecesidadColores = new ArrayList<>();
        Boolean variosColores = Boolean.FALSE;
        List<String> colores = new ArrayList<>();
        String resultadosMochi = ""; 
        
        listadoBarrasProyecto = getBarrasByProyecto(referenciaProyecto);
        String otros_lacables = proyectoServices.getOtrosLacablesProyectoByReferencia(referenciaProyecto);
        String sinColor = "    ";
        
        if (listadoBarrasProyecto!=null){
            for (Barras b: listadoBarrasProyecto){
                if (!colores.contains(b.getColor()) && !b.getIdentificadorUnicoBarra().contains("REST") && !b.getColor().equals(sinColor) ){ // Ojo este codigo sinColor es para que la barra falsa aluminio en extra no lo cuente como un color distinto.
                    colores.add(b.getColor());
                }
            }
        }
        if (colores.size()>=1){
            if (colores.size()>1){
                variosColores = Boolean.TRUE;
            }    
            for (int i=0; i<colores.size(); i++){
                barrasNecesidadColores.add(i,new ArrayList<Barras>());
                for (Barras b: listadoBarrasProyecto){
                    if (b.getColor().equals(colores.get(i))){
                        barrasNecesidadColores.get(i).add(b);
                    }
                }
                Mochilificador mochi = new Mochilificador(barrasNecesidadColores.get(i),stockBarrasService, barraServices, variosColores, 2, logBarraServices, otros_lacables);         
                resultadosMochi += mochi.ejecutar();
                
            }
        }
        return resultadosMochi;
    
    }

    @Override
    public String mochilificarBarrasByProyecto(String referenciaProyecto, Integer emisor) throws Exception{
        List<Barras> listadoBarrasProyecto ; 
        List<List<Barras>> barrasNecesidadColores = new ArrayList<>();
        Boolean variosColores = Boolean.FALSE;
        List<String> colores = new ArrayList<>();
        String resultadosMochi = ""; 
        
        listadoBarrasProyecto = getBarrasByProyecto(referenciaProyecto);
        String otros_lacables = proyectoServices.getOtrosLacablesProyectoByReferencia(referenciaProyecto);
        String sinColor = "    ";
        
        if (listadoBarrasProyecto!=null){
            for (Barras b: listadoBarrasProyecto){
                if (!colores.contains(b.getColor()) && !b.getIdentificadorUnicoBarra().contains("REST") && !b.getColor().equals(sinColor)){
                    colores.add(b.getColor());
                }
            }
        }
        if (colores.size()>=1){
            if (colores.size()>1){
                variosColores = Boolean.TRUE;
            }    
            for (int i=0; i<colores.size(); i++){
                barrasNecesidadColores.add(i,new ArrayList<Barras>());
                for (Barras b: listadoBarrasProyecto){
                    if (b.getColor().equals(colores.get(i))){
                        barrasNecesidadColores.get(i).add(b);
                    }
                }
                Mochilificador mochi = new Mochilificador(barrasNecesidadColores.get(i),stockBarrasService, barraServices, variosColores, emisor, logBarraServices, otros_lacables);         
                resultadosMochi += mochi.ejecutar();
            }
        }
        return resultadosMochi;
    }
    
    
    @Override
    public String agregaRestosByProyecto (String referenciaProyecto, Boolean automantenimiento, Integer codigoActividad) throws Exception{
        List<Barras> listadoBarrasProyecto;
        List<List<Barras>> barrasNecesidadColores = new ArrayList<>();
        Boolean variosColores = Boolean.FALSE;
        List<String> colores = new ArrayList<>();
        String logAgregaRestos = "";
        
        listadoBarrasProyecto = getBarrasByProyecto(referenciaProyecto);
        
        // Aqui si se envian todos los colores para que se agregen restos de cada color si es necesario.
        if (listadoBarrasProyecto!=null){
            for (Barras b: listadoBarrasProyecto){
                if (!colores.contains(b.getColor())){
                    colores.add(b.getColor());
                }
            }
        }
        if (colores.size()>=1){
            if (colores.size()>1){
                variosColores = Boolean.TRUE;
            }    
            for (int i=0; i<colores.size(); i++){
                barrasNecesidadColores.add(i,new ArrayList<Barras>());
                for (Barras b: listadoBarrasProyecto){
                    if (b.getColor().equals(colores.get(i))){
                        barrasNecesidadColores.get(i).add(b);
                    }
                }
                Mochilificador mochi = new Mochilificador(barrasNecesidadColores.get(i),stockBarrasService, barraServices, variosColores, 2, logBarraServices,"");         
                logAgregaRestos += mochi.agregaRestos(barrasNecesidadColores.get(i), automantenimiento, codigoActividad) + " ";
                
            }
        } 
        
        
        /*if(listadoBarrasProyecto!=null && listadoBarrasProyecto.size()>0){
            Mochilificador mochi = new Mochilificador(listadoBarrasProyecto,stockBarrasService, barraServices, false);
            return mochi.agregaRestos(listadoBarrasProyecto, automantenimiento, codigoActividad);
        }else{
            throw new Exception("No hay barras del proyecto: " + referenciaProyecto + " para agregar restos.");
        }*/
        return logAgregaRestos;
    }    
    
    @Override
    public String liberaBarrasByProyecto (String referenciaProyecto) throws Exception{
        List<Barras> listadoBarrasProyecto;
        List<List<Barras>> barrasNecesidadColores = new ArrayList<>();
        Boolean variosColores = Boolean.FALSE;
        List<String> colores = new ArrayList<>();
        String log ="";
        
        listadoBarrasProyecto = getBarrasByProyecto(referenciaProyecto);
        
        if (listadoBarrasProyecto!=null){
            for (Barras b: listadoBarrasProyecto){
                if (!colores.contains(b.getColor()) && !b.getIdentificadorUnicoBarra().contains("REST")){
                    colores.add(b.getColor());
                }
            }
        }
        if (colores.size()>=1){
            if (colores.size()>1){
                variosColores = Boolean.TRUE;
            }    
            for (int i=0; i<colores.size(); i++){
                barrasNecesidadColores.add(i,new ArrayList<Barras>());
                for (Barras b: listadoBarrasProyecto){
                    if (b.getColor().equals(colores.get(i))){
                        barrasNecesidadColores.get(i).add(b);
                    }
                }
                Mochilificador mochi = new Mochilificador(barrasNecesidadColores.get(i),stockBarrasService, barraServices, variosColores, 2, logBarraServices, "");         
                log += mochi.liberaBarras(barrasNecesidadColores.get(i));
                
                
            }
        }
        
        barraServices.deleteBarrasByProyecto(referenciaProyecto);
        return log;
    }    
    
    @Override
    public String preparaBarrasByProyecto (String referenciaProyecto) throws Exception{
        List<Barras> listadoBarrasProyecto;
        List<List<Barras>> barrasNecesidadColores = new ArrayList<>();
        Boolean variosColores = Boolean.FALSE;
        List<String> colores = new ArrayList<>();
        String log ="";
        
        listadoBarrasProyecto = getBarrasByProyecto(referenciaProyecto);
        
        if (listadoBarrasProyecto!=null){
            for (Barras b: listadoBarrasProyecto){
                if (!colores.contains(b.getColor())){
                    colores.add(b.getColor());
                }
            }
        }
        if (colores.size()>=1){
            if (colores.size()>1){
                variosColores = Boolean.TRUE;
            }    
            for (int i=0; i<colores.size(); i++){
                barrasNecesidadColores.add(i,new ArrayList<Barras>());
                for (Barras b: listadoBarrasProyecto){
                    if (b.getColor().equals(colores.get(i))){
                        barrasNecesidadColores.get(i).add(b);
                    }
                }
                Mochilificador mochi = new Mochilificador(barrasNecesidadColores.get(i),stockBarrasService, barraServices, variosColores, 2, logBarraServices, "");         
                log += mochi.preparaBarras(barrasNecesidadColores.get(i));
                
                
            }
        }
        
        return log;
    }    
    
    // Metodos auxiliares
    @Override
    /**
     * Si encuentra barras de tipo PERFIL nos devuelve un string con PERFIL06,PERFIL08, PERFIL10 o PEFIL12
     */
    public String grosorPerfil(List<Barras> auxBarrasNecesidad) throws Exception {
        if (auxBarrasNecesidad !=null && !auxBarrasNecesidad.isEmpty()){
            for (Barras b:auxBarrasNecesidad){
                if (b.getTipoPerfil().contains("PERF") || b.getTipoPerfil().contains("PEROD")){
                    return b.getTipoPerfil();
                }
                
            }
        } 
        return "NO";
    }
    
    @Override
    /**
     * Si encuentra barras de tipo PERFIL nos devuelve un string con PERFIL06,PERFIL08, PERFIL10 o PEFIL12
     */
    public String colorAluminio(List<Barras> auxBarrasNecesidad) throws Exception {
        String sinColor = "    ";
        if (auxBarrasNecesidad !=null && !auxBarrasNecesidad.isEmpty()){
            for (Barras bar : auxBarrasNecesidad){
                if (!bar.getColor().equals(sinColor)){
                    return (bar.getColor());
                }
            }
        }
        return "ERROR";
    }
    
    @Override
    /**
     * Dado el color, el lacado y el tipo de perfil del sistema, indica si hay que lacar o no
     */
    public Boolean paraLacar (String color, String tipoPerf, String lacadoForzado, List<String> coloresStock) throws Exception{
        /*if (tipoPerf.equals("NO")){ // Si dado un mochilificado de barras no se ha encontrado un tipo de perfil valido, por ejemplo, si manualmente ponemos un compensador de un color concreto, 
            // en esa lista de barras no hay perfiles y el metodo nos devuelve "NO", para controlar que no se manden a lacar barras de stock por esta casuistica, si viene con "NO", lo incializamos a "PERFIL10"
            tipoPerf = "PERFIL10";
        }*/
        if(lacadoForzado.equals("ENTERAS") || lacadoForzado.equals("DEGRUPO")){
            return Boolean.TRUE;
        }
        
        if (color.equals("XXXX")){
            return Boolean.FALSE;
        } else if (lacadoForzado.equals("NO")){
            if (color.equals("9003") || (coloresStock.contains(color))){
                return Boolean.FALSE;
            } else {
                return Boolean.TRUE;
            }
        } else {
            return Boolean.TRUE;
        }
       
    }
    
    @Override
    /**
     * Nos devuelve el color asociado de las barras de stock al proyecto. Usamos el color ficticio CRUSOP para indicar que es un crudo que se laca en barras enteras pr lo que no genera
     * restos aprovechables.
     */
    public String adaptaColorALacado(String color, String lacadoForzado, Boolean paraLacar) throws Exception{
        if (paraLacar){ //coloresStock.contains(color) && !color.equals("XXXX")){
            if (lacadoForzado.equals("NO")){
                if (color.startsWith("AN0") || color.startsWith("MA")){
                    return "CRUSOP";
                } else {
                    return "CRUDO";
                }
            } else {
                if (lacadoForzado.contains("ENTERAS")){
                    return "CRUSOP";
                } else {   // Al ser lacado forzado y no ser ENTERAS es GRUPOS 
                    return "CRUDO";
                } 
            }    
        } else {
            
            return (color.equals("XXXX") ? "CRUDO": color);
        }
        
    }
    
    @Override
    /**
     * En la etiequeta tagD de las barras, indicamos desde Appglass si existe lacado forzado, siendo #L1 GRUPOS y #L2 ENTERAS
     */
    public String comprobarSiHayLacadoForzado(List<Barras> auxListaNecesidades) throws Exception {
        if (auxListaNecesidades != null && !auxListaNecesidades.isEmpty()) {
            String tipoSistema = auxListaNecesidades.get(0).getTipoSistema();
            Boolean hayMicaEmbutido = Boolean.FALSE;
            if (tipoSistema.equals("CORREDERA_MICA")) {
                for (Barras ba : auxListaNecesidades) {
                    if (ba.getTipoPerfil().startsWith("CINE")) { // hay barra embutida
                        hayMicaEmbutido = Boolean.TRUE;
                    }
                    if (hayMicaEmbutido) {
                        break;
                    }
                }
            }
            for (Barras barra : auxListaNecesidades) {
                if (barra.getTipoSistema().startsWith("CORREDERA")) {
                    if (barra.getTagD() != null && !barra.getTagD().isEmpty() && (barra.getTagD().contains("#L1") || barra.getTagD().contains("#L2"))) {
                        return ("ENTERAS");
                    }
                    if (barra.getTipoSistema().contains("MICA")) {
                        if (barra.getColor() != null) {
                            if (hayMicaEmbutido && barra.getColor().equals("7016T")) {
                                return ("ENTERAS");
                            }
                            if (!barra.getColor().equals("9003") && !barra.getColor().equals("XXXX") && !barra.getColor().equals("7016T")) {
                                return ("ENTERAS");
                            }
                        }
                    } else {
                        if (barra.getColor() != null && (!barra.getColor().equals("9003") && !barra.getColor().equals("7016") && !barra.getColor().equals("XXXX"))) {
                            return ("ENTERAS");
                        }
                    }
                } else if (barra.getTipoSistema().contains("INFINIA")) {
                    if (barra.getTagD() != null && !barra.getTagD().isEmpty() && (barra.getTagD().contains("#L1") || barra.getTagD().contains("#L2"))) {
                        return ("ENTERAS");
                    }
                    if (barra.getColor() != null && (!barra.getColor().equals("9003") && !barra.getColor().equals("7016T") && !barra.getColor().equals("XXXX"))) {
                        return ("ENTERAS");
                    }

                }
                if (barra.getTagD() != null && !barra.getTagD().isEmpty()) {
                    if (barra.getTagD().contains("#L1")) {
                        return ("DEGRUPO");
                    }
                    if (barra.getTagD().contains("#L2")) {
                        return ("ENTERAS");
                    }
                }
            }
        }
        return "NO";
    }
    
    @Override
    /**
     * Colores en stock
     */
    public List<String> inicializarColores(String tipoSistema, Boolean status10mm) throws Exception {
        List<String> auxColores = new ArrayList<>();
        if (tipoSistema != null && !tipoSistema.isEmpty()) {
            switch (tipoSistema) {
                case "CORTINA_STATUS":
                    auxColores.add("9003");  //Blanco
                    if (status10mm){
                        auxColores.add("9010");  // Blanco (italia)
                        auxColores.add("8017");  // Chocolate
                        auxColores.add("7035"); // Añadido 121121
                        auxColores.add("7016"); // añadido 121121
                        auxColores.add("XXXX"); // Crudo
                        auxColores.add("AN0001"); // Anodizado Bronze
                        auxColores.add("AN0002"); // " Plata
                        auxColores.add("AN0004"); // " Negro Mate
                        auxColores.add("AN0006"); // " Acero Inox
                        auxColores.add("1013"); // Blanco Perla
                        auxColores.add("ES0006"); // AÑADIDO AL MOCHI EL 06102025 
                        //auxColores.add("9005"); // Es el 9005 Mate que sustituye al ES0006
                    }
                    break;
                case "CORTINA_CRUISER":
                    auxColores.add("XXXX");
                    auxColores.add("9003");
                    break;
                case "CORREDERA_MICA":
                    auxColores.add("XXXX");
                    auxColores.add("9003");
                    auxColores.add("7016T"); // OJO HIBRIDO, LOS EMBUTIDOS NO VAN
                    break;
                case "CORREDERA":    
                case "CORREDERA_JADE":
                    auxColores.add("XXXX");
                    auxColores.add("9003");
                    auxColores.add("7016");
                    break;    
                case "MOTORIZADA":
                    auxColores.add("XXXX");
                    break;
                case "BARANDILLA":
                    auxColores.add("XXXX");
                    auxColores.add("AN0002");
                    break;
                default:
            }
        }
        return auxColores;
    }
    
    @Override
    /**
     * Lista de perfiles que se mochilifican
     */
    public List<String> inicializarPerfilesMochilificables() throws Exception{
        List<String> auxPerfiles = new ArrayList<>();
        auxPerfiles.add("CARRIL");
        auxPerfiles.add("CARRIL_INV");
        auxPerfiles.add("CARRILPLUS");
        auxPerfiles.add("CARRILPLUS_INV");
        auxPerfiles.add("COMPENSADOR");
        auxPerfiles.add("CICEN");
        auxPerfiles.add("CICEN_INV");
        auxPerfiles.add("CIEMB");
        auxPerfiles.add("CIEMB_INV");
        auxPerfiles.add("CIDOB");
        auxPerfiles.add("CIDOB_INV");
        auxPerfiles.add("CSSIMP");
        auxPerfiles.add("CSSIMP_INV");
        auxPerfiles.add("CSDOB");
        auxPerfiles.add("CSDOB_INV");
        auxPerfiles.add("PREMATE");
        auxPerfiles.add("CIN3R");
        auxPerfiles.add("CINE3R");
        auxPerfiles.add("CSU3R");
        auxPerfiles.add("CIN4R");
        auxPerfiles.add("CINE4R");
        auxPerfiles.add("CSU4R");
        auxPerfiles.add("CIN5R");
        auxPerfiles.add("CINE5R");
        auxPerfiles.add("CSU5R");
        auxPerfiles.add("PVMICA");
        auxPerfiles.add("PARRAPAN");
        auxPerfiles.add("PASPAN2FIJ");
        auxPerfiles.add("PASPANFIJ");
        auxPerfiles.add("PASPANMOV");
        auxPerfiles.add("PHOREXT");
        auxPerfiles.add("PVERPANF");
        auxPerfiles.add("PVERPAN");
        auxPerfiles.add("PHORBASE");
        auxPerfiles.add("CAJMOTOR");
        auxPerfiles.add("REDCR10");
        auxPerfiles.add("EMBRAIL");
        auxPerfiles.add("RVERINT");
        auxPerfiles.add("RVERMED");
        auxPerfiles.add("RVERBASE");
        return auxPerfiles;
    }
    
    @Override
    public List<String> inicializarOtrasBarrasNoMochilificables() throws Exception{
        List<String> auxPerfiles = new ArrayList<>();
        auxPerfiles.add("PVROD22");
        auxPerfiles.add("PXROD22");
        auxPerfiles.add("PU");
        auxPerfiles.add("PLT");
       
        return auxPerfiles;
    }
    
    
}
