package com.todocristal.fabrica.webservice.dao;

import com.todocristal.fabrica.webservice.model.Barras;
import com.todocristal.fabrica.webservice.model.StockBarras;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.ArrayList;
//import javafx.util.converter.BigIntegerStringConverter;
import javax.transaction.Transactional;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
//AYUDA
//https://docs.jboss.org/hibernate/orm/3.3/reference/en/html/querycriteria.html

@Repository
@Transactional  
public class StockBarrasDaoImpl implements StockBarrasDao {
    
    @Autowired
    SessionFactory sessionFactory;

    Session session = null;
    //Session sessionsb = null;
    Transaction tx = null;

    @Override
    public boolean addBarra(StockBarras barra, boolean versionGestor) throws Exception {


        session = sessionFactory.openSession();                    
        tx = session.beginTransaction();                
        session.saveOrUpdate(barra);
        tx.commit();
        session.close();

        return false;
    }

    @Override
    public StockBarras getBarraById(long id) throws Exception {
            session = sessionFactory.openSession();	
            tx = session.beginTransaction();
            StockBarras barra = (StockBarras) session.get(StockBarras.class, id);
            tx.commit();
            session.close();
            return barra;
    }
    /*
        --------   OBTENEMOS INFORMACIÓN SEGÚN LA REFERENCIA, ASÍ PODEMOS OBTENER EL ID PARA TRATAR EL PROYECTO Y LAS TAREAS.
        EN LA SINCRONIZACIÓN.
    */
    @Override
    public List<StockBarras> getBarrasByProyecto(Integer referencia) throws Exception{
            session = sessionFactory.openSession();	
            tx = session.beginTransaction();

            Criteria crit = session.createCriteria(StockBarras.class)
                .createAlias("proyecto", "p")
                .add( Restrictions.like("p.referencia", referencia));
            
            List<StockBarras> barras = crit.list();

            tx.commit();
            session.close();
            return barras;

    }

    @Override
    public List<StockBarras> getStockBarras(){
        session = sessionFactory.openSession();
        tx = session.beginTransaction();
        List<StockBarras> barra = session.createCriteria(StockBarras.class).list();
        tx.commit();
        session.close();

        return barra;
    }
    
    @Override
    public List<StockBarras> getStockBarras(String color, String tipoSistema) {
        session = sessionFactory.openSession();
        tx = session.beginTransaction();
        if (tipoSistema.contains("CORTINA")){
            tipoSistema = "CORTINA_STATUS";
        }
        List<StockBarras> barra = session.createCriteria(StockBarras.class)
                .add(Restrictions.eq("tipoSistema", tipoSistema))
                .add(Restrictions.eq("color", color))
                .list();
        tx.commit();
        session.close();

        return barra;
    }
    
    @Override
    public List<StockBarras> getStockBarras(String color, String tipoSistema, String perfil){
        session = sessionFactory.openSession();
        tx = session.beginTransaction();
        List<StockBarras> barra = session.createCriteria(StockBarras.class)
                .add(Restrictions.eq("tipoSistema", tipoSistema))
                .add(Restrictions.eq("color", color))
                .add(Restrictions.eq("tipoPerfil", perfil))
                .list();
        tx.commit();
        session.close();

        return barra;
    }
    
    
    @Override
    public boolean deleteBarra(long id) throws Exception {
            session = sessionFactory.openSession();
            Object o = session.load(StockBarras.class, id);
            tx = session.getTransaction();
            session.beginTransaction();
            ((StockBarras)o).setUnidades(((StockBarras)o).getUnidades() > 0 ? ((StockBarras)o).getUnidades()-1: 0);
            session.update(o);
            tx.commit();
            return false;   //?
    }
    
    @Override
    public boolean deleteBarra(StockBarras barra) throws Exception {
            session = sessionFactory.openSession();
            
            tx = session.getTransaction();
            session.beginTransaction();            
            session.update(barra);
            tx.commit();
            return false;   //?
    }

    @Override
    public boolean updateBarras(List<StockBarras> barras) throws Exception{
            session = sessionFactory.openSession();
            tx = session.beginTransaction();

            for(StockBarras sb : barras){
                session.update(sb);
            }

            tx.commit();
            session.close();

            return false;   //?
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
    public List<StockBarras> getBarrasEnCurso() throws Exception{
        List<StockBarras> barrasEnCurso=null;
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
    public boolean deleteBarrasByProyecto(Integer referenciaProyecto) {
        session = sessionFactory.openSession();
        Criteria crit = session.createCriteria(StockBarras.class)
            .createAlias("proyecto", "p")
            .add( Restrictions.like("p.referencia", referenciaProyecto));
            
            List<StockBarras> listaBarras = crit.list();
            for(StockBarras b : listaBarras){
                System.out.println(b.getLongitud());
                session.delete(b);
            }
            tx = session.getTransaction();
            session.beginTransaction();
            
            tx.commit();
            return false;
    }

    @Override
    public boolean agregarNuevoGrupoDadoGrupoBase(String nuevoColor, List<StockBarras> listadoGrupoBase){
        session = sessionFactory.openSession();
        
        for(StockBarras sb : listadoGrupoBase){
            if(sb.getLongitud()>6200.0){
                sb.setUnidades(1000);
            }else{
                sb.setUnidades(0);
            }
            sb.setColor(nuevoColor);
            sb.setMermaForzar(300.0);
            sb.setStockForzar(10);
            System.out.println("Nuevo grupo stock:" + sb.getColor()+" "+sb.getTipoPerfil() + " "+ sb.getTipoSistema());
            session.save(sb);
        }
        tx = session.getTransaction();
        session.beginTransaction();

        tx.commit();
        return Boolean.TRUE;
    }
    
    
    
    /* 30/04/2019
    private Integer obtenerUnidadesStockBarra(Session session, long id){
        StockBarras barra = (StockBarras) session.get(StockBarras.class, id);        
        return barra.getUnidades();
        
    }*/
    /**
     * 30/04/2019
     * @param listaBarrasFiltroColorSistema (listado de las barras de stock correspondientes a un color y sistema.)
     * @param barra Tipo o familia de barra
     * @param grupoLongitud Longitud buscada
     * @return 
     
    private StockBarras obtenerBarraStock(StockBarras barra, Double grupoLongitud){
        List<StockBarras> listaBarrasFiltroColorSistema = getStockBarras(barra.getColor(), barra.getTipoSistema());
        
        for(StockBarras b : listaBarrasFiltroColorSistema){
            if(b.getTipoPerfil().equals(barra.getTipoPerfil()) && barra.getLongitud().equals(grupoLongitud)){
                return b;
            }
        }
        return null;
    }*/
    
    
    
    

   
        
}
