package com.todocristal.fabrica.webservice.model;

import java.io.Serializable;
import java.util.Comparator;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;



@Entity(name= "ForeignKeyAssoBarrasEntity")
@Table(name = "barras", uniqueConstraints = {@UniqueConstraint(columnNames ="id")})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

public class Barras implements Serializable, Comparable<Barras> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "longitud", nullable = false)
    private Double longitud;  // Longitud de la barra una vez cortada
        
    
    @ManyToOne                
    private Proyectos proyecto;

    
    @Column(name = "tipo_perfil")
    private String tipoPerfil;  // Carril, compensador, perfil, etc...
    @Column(name = "posicion")
    private String posicion; // Posicion relativa en su seccion, superior, inferior, etc...
    @Column(name = "tipo_sistema")
    private String tipoSistema; // Cortina, corredera, etc...
    @Column(name = "identificador_barra")
    private String identificadorUnicoBarra; //Codigo univoco que define a una barra se forma
    
    @Column(name = "color")
    private String color;    // Color de la barra
    @Column(name = "grosor")
    private Integer grosor;  // Grosor del perfil para el corte
    @Column(name = "unidades")
    private Integer unidades;
    @Column(name = "pertenece_a_seccion")
    private Integer perteneceASeccion; // Asocia la barra a una seccion en concreto
    @Column(name = "invetir")
    private Boolean invertir; // Se utiliza como boolean para saber si hay que invertir una barra asimetrica
    
    @Column(name = "angulo_derecho")
    private Double anguloDerecho; 
    @Column(name = "angulo_izquierdo")
    private Double anguloIzquierdo;
    @Column(name = "longitud_interior")
    private Double longitudInterior;
    @Column(name = "longitud_exterior")
    private Double longitudExterior; 
    @Column(name = "tagA")
    private String tagA; // Referencia del proyecto
    @Column(name = "tagB")
    private String tagB; // Seccion y posicion
    @Column(name = "tagC")
    private String tagC; // Descripcion generica del perfil, si es el de la puerta, si es el de la puerta movil, etc... numero de barra dentro del total de las barras de esa seccion 1 de tantos, 2 de tantos....
    @Column(name = "tagD")
    private String tagD;
    @Column(name = "editada")
    private Boolean editada;
    @Column(name = "mochilificada")
    private Integer mochilificada;      //Relaci�n de barra necesidad con stockbarras. 0 (no hay relaci�n), 1 (relaci�n 1 a 1), +2 (de muchos a uno). Para descuento de stock s�lo es v�lido el valor 1.
    @Column(name = "grupo_stock")
    private Integer grupoStock;     //Cuando es barra nueva 0, cualquier otro caso relaci�n con el grupo ID.
    @Column(name = "identificador_stock")
    private Integer identificador_stock;
    @Column(name = "merma")
    private Double merma;
    @Column(name = "resto")
    private Double resto;
    
    
   //@ManyToOne
    //private Perfiles perfil;
    /*
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROYECTO_ID")
    @Fetch(FetchMode.SUBSELECT)
    private List<Actividades> listaActividades;
    */
    
    /*@Transient
    @OneToMany(mappedBy = "barra")
    private List<Mermas> mermas;
    */
    
    public Integer sePuedeCortarEnMaquinaNueva(){
        // Valores seran, 0 para se puede, 1 para se puede pero inviertiendo, -1 para no se puede ni invirtiendo.
        Boolean hayAnguloIzquierdoMayor90 = Boolean.FALSE;
        Boolean hayAnguloIzquierdoMenor90 = Boolean.FALSE;
        
        Boolean hayAnguloDerechoMenor90 = Boolean.FALSE;
        Boolean hayAnguloDerechoMayor90 = Boolean.FALSE;
        
        if (this.anguloIzquierdo!=null && this.anguloIzquierdo!=90.0){
            if (this.anguloIzquierdo>90.0){ // la maquina nueva solo corta hacia exterior, por lo que valores de mas de 90 en el angulo izquierdo, suponen que el disco tiene que ir hacia el interior y no puede
                hayAnguloIzquierdoMayor90 = Boolean.TRUE;
                //return Boolean.FALSE;
            } else {
                hayAnguloIzquierdoMenor90 = Boolean.TRUE;
            }
        }
        if (this.anguloDerecho!=null && this.anguloDerecho!=90.0){
            if (this.anguloDerecho>90.0){ // caso opuesto en el angulo derecho.
                hayAnguloDerechoMayor90 = Boolean.TRUE;
                //return Boolean.FALSE;
            } else {
                hayAnguloDerechoMenor90 = Boolean.TRUE;
            }
        }
        if ((hayAnguloIzquierdoMayor90 && hayAnguloDerechoMenor90) || (hayAnguloIzquierdoMenor90 && hayAnguloDerechoMayor90)){
            // No podemos voltearlo ya que convertimos el otro angulo en problematico
            return -1;
        }
        if (hayAnguloIzquierdoMayor90 || hayAnguloDerechoMayor90){
            return 1;
        }
        return 0;
    }
    
    public final void invertirAngulosBarra (){
        Double longOL = this.longitudExterior;
        Double longIL = this.longitudInterior;
        Double auxAnguloIzq = this.anguloIzquierdo;
        Double auxAnguloDcha = this.anguloDerecho;
        this.anguloIzquierdo=(180 - auxAnguloIzq);
        this.anguloDerecho=(180 - auxAnguloDcha);
        this.longitudExterior=(longIL);
        this.longitudInterior=(longOL);
       
    }
    
        public final void invertirAngulosBarraParaMaquinaX2(){
        Double longOL = this.longitudExterior;
        Double longIL = this.longitudInterior;
        Double auxAnguloIzq = this.anguloIzquierdo;
        Double auxAnguloDcha = this.anguloDerecho;
        this.anguloIzquierdo=(180 - auxAnguloDcha);
        this.anguloDerecho=(180 - auxAnguloIzq);
        this.longitudExterior=(longIL);
        this.longitudInterior=(longOL);
        
    }
    

    public long getId() {
            return id;
    }

    public void setId(long id) {
            this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Proyectos getProyecto() {
        return proyecto;
    }

    public void setProyecto(Proyectos proyecto) {
        this.proyecto = proyecto;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public String getTipoPerfil() {
        return tipoPerfil;
    }

    public void setTipoPerfil(String tipoPerfil) {
        this.tipoPerfil = tipoPerfil;
    }

    public String getPosicion() {
        return posicion;
    }

    public void setPosicion(String posicion) {
        this.posicion = posicion;
    }

    public String getTipoSistema() {
        return tipoSistema;
    }

    public void setTipoSistema(String tipoSistema) {
        this.tipoSistema = tipoSistema;
    }

    public String getIdentificadorUnicoBarra() {
        return identificadorUnicoBarra;
    }

    public void setIdentificadorUnicoBarra(String identificadorUnicoBarra) {
        this.identificadorUnicoBarra = identificadorUnicoBarra;
    }

    public Integer getGrosor() {
        return grosor;
    }

    public void setGrosor(Integer grosor) {
        this.grosor = grosor;
    }

    public Integer getUnidades() {
        return unidades;
    }

    public void setUnidades(Integer unidades) {
        this.unidades = unidades;
    }

    public Integer getPerteneceASeccion() {
        return perteneceASeccion;
    }

    public void setPerteneceASeccion(Integer perteneceASeccion) {
        this.perteneceASeccion = perteneceASeccion;
    }

    public Boolean getInvertir() {
        return invertir;
    }

    public void setInvertir(Boolean invertir) {
        this.invertir = invertir;
    }

    public Double getAnguloDerecho() {
        return anguloDerecho;
    }

    public void setAnguloDerecho(Double anguloDerecho) {
        this.anguloDerecho = anguloDerecho;
    }

    public Double getAnguloIzquierdo() {
        return anguloIzquierdo;
    }

    public void setAnguloIzquierdo(Double anguloIzquierdo) {
        this.anguloIzquierdo = anguloIzquierdo;
    }

    public Double getLongitudInterior() {
        return longitudInterior;
    }

    public void setLongitudInterior(Double longitudInterior) {
        this.longitudInterior = longitudInterior;
    }

    public Double getLongitudExterior() {
        return longitudExterior;
    }

    public void setLongitudExterior(Double longitudExterior) {
        this.longitudExterior = longitudExterior;
    }

    public String getTagA() {
        return tagA;
    }

    public void setTagA(String tagA) {
        this.tagA = tagA;
    }

    public String getTagB() {
        return tagB;
    }

    public void setTagB(String tagB) {
        this.tagB = tagB;
    }

    public String getTagC() {
        return tagC;
    }

    public void setTagC(String tagC) {
        this.tagC = tagC;
    }

    public String getTagD() {
        return tagD;
    }

    public void setTagD(String tagD) {
        this.tagD = tagD;
    }

    public Boolean getEditada() {
        return editada;
    }

    public void setEditada(Boolean editada) {
        this.editada = editada;
    }

    public Integer getMochilificada() {
        return mochilificada;
    }

    public void setMochilificada(Integer mochilificada) {
        this.mochilificada = mochilificada;
    }

    public Integer getGrupoStock() {
        return grupoStock;
    }

    public void setGrupoStock(Integer grupoStock) {
        this.grupoStock = grupoStock;
    }

    public Integer getIdentificador_stock() {
        return identificador_stock;
    }

    public void setIdentificador_stock(Integer identificador_stock) {
        this.identificador_stock = identificador_stock;
    }

    public Double getMerma() {
        return merma;
    }

    public void setMerma(Double merma) {
        this.merma = merma;
    }

    public Double getResto() {
        return resto;
    }

    public void setResto(Double resto) {
        this.resto = resto;
    }
    
        /*@Override INTENTO COMPARAR CON COMPARATOR
    public int compare(Barras b1, Barras b2) {
        if( b1.getTipoPerfil().equals(b2.getTipoPerfil()) ){
            return b1.getLongitud().compareTo(b2.getLongitud());
        }else{
            return -1;  //menor por defecto 
        }
    }*/

    /*@Override
    public int compareTo(Barras b) {
        if(b.getLongitud() < longitud ){
            return -1;
        }
        if(b.getLongitud() > longitud ){
            return 1;
        }
        
        return 0;
    }*/
    public Barras barraDesdeTxt(String barraString){
        String split = ";";
        String[] campos;
        if (barraString!=null && !barraString.isEmpty()){
            campos = barraString.split(split);
            this.longitud = Double.valueOf(campos[0]);
            this.tipoPerfil = campos[1];
            this.posicion = campos[2];
            this.tipoSistema = campos[3];
            this.identificadorUnicoBarra = campos[4];
            this.color = campos[5];
            this.grosor = Integer.parseInt(campos[6]);
            this.unidades = Integer.parseInt(campos[7]);
            this.perteneceASeccion = Integer.parseInt(campos[8]);
            this.invertir = Boolean.valueOf(campos[9]);
            this.anguloDerecho = Double.valueOf(campos[10]); 
            this.anguloIzquierdo = Double.valueOf(campos[11]); 
            this.longitudInterior = Double.valueOf(campos[12]);
            this.longitudExterior = Double.valueOf(campos[13]); 
            this.tagA = campos[14];
            this.tagB = campos[15];
            this.tagC = campos[16];
            this.tagD = campos[17];
            this.editada = Boolean.valueOf(campos[18]);
            this.mochilificada = Integer.parseInt(campos[19]);
            this.grupoStock = Integer.parseInt(campos[20]);
            this.identificador_stock = Integer.parseInt(campos[21]);
            this.merma = Double.valueOf(campos[22]);
            this.resto = Double.valueOf(campos[23]);
        }
        return this;
    }
    
    @Override
    public int compareTo(Barras b) {

      // Para carriles y compensadores, se ordena de menor a mayor para poder gastar mas stock de barras peque�as.
            if (b.getLongitud() < longitud) {
                return -1;
            } else if (b.getLongitud() > longitud) {
                return 1;
            }
            return 0;
        
    }

    @Override
    public String toString(){
        return(
            (this.longitud!=null ? this.longitud.toString() :  "")+";"+
            this.tipoPerfil +";"+
            this.posicion+";"+
            this.tipoSistema+";"+
            this.identificadorUnicoBarra+";"+
            this.color+";"+
            (this.grosor!=null ? this.grosor.toString() : "")+";"+
            (this.unidades !=null ? this.unidades.toString() :"")+";"+
            (this.perteneceASeccion!=null ? this.perteneceASeccion.toString() : "")+";"+
            (this.invertir!=null ? this.invertir.toString() : "") +";"+
            (this.anguloDerecho!=null ? this.anguloDerecho.toString() : "") +";"+ 
            (this.anguloIzquierdo!=null ? this.anguloIzquierdo.toString() : "") +";"+
            (this.longitudInterior!=null ? this.longitudInterior.toString() : "") +";"+
            (this.longitudExterior!=null ? this.longitudExterior.toString() : "")+";"+
            this.tagA+";"+
            this.tagB+";"+
            this.tagC+";"+
            this.tagD+";"+
            (this.editada!=null ? this.editada.toString() : "")  +";"+
            (this.mochilificada!=null ? this.mochilificada.toString() : "") +";"+
            (this.grupoStock!=null ? this.grupoStock.toString() : "") +";"+
            (this.identificador_stock!=null ? this.identificador_stock.toString() : "") +";"+
            (this.merma!=null ? this.merma.toString() : "")   +";"+
            (this.resto!=null ? this.resto.toString() : "")   +";"+
                "\n");
    }

    public static final Comparator<Barras> BY_TIPO_PERFIL
            = new Comparator<Barras>() {
        @Override
        public int compare(Barras b1, Barras b2) {
            return b1.getTipoPerfil().compareTo(b2.getTipoPerfil());
        }
    };
    
    public static final Comparator<Barras> BY_TIPO_PERFIL_SEC
            = new Comparator<Barras>() {
        @Override
        public int compare(Barras b1, Barras b2) {
            // Perfil
            if (b1.getTipoPerfil() == null && b2.getTipoPerfil() != null) return -1;
            if (b1.getTipoPerfil() != null && b2.getTipoPerfil() == null) return 1;
            if (b1.getTipoPerfil() != null && b2.getTipoPerfil() != null) {
                int cmp = b1.getTipoPerfil().compareTo(b2.getTipoPerfil());
                if (cmp != 0) return cmp;
            }
             // Secci�n
            if (b1.getPerteneceASeccion() == null && b2.getPerteneceASeccion() != null) return -1;
            if (b1.getPerteneceASeccion() != null && b2.getPerteneceASeccion() == null) return 1;
            if (b1.getPerteneceASeccion() != null && b2.getPerteneceASeccion() != null) {
                return b1.getPerteneceASeccion().compareTo(b2.getPerteneceASeccion());
            }
            return 0;
        }
    };
    
       
    
}
