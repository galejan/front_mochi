package com.todocristal.fabrica.webservice.model;

import java.io.Serializable;


import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;



@Entity(name= "ForeignKeyAssoProyectosEntity")
@Table(name = "proyectos", uniqueConstraints = {@UniqueConstraint(columnNames ="id"), @UniqueConstraint(columnNames = "referencia")})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

public class Proyectos implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "id", nullable = false)
	private long id;

	@Column(name = "referencia", nullable = false)
	private String referencia;
        
        @Column(name = "nombre")
	private String nombre;

	@Column(name = "fecha", nullable = false, length = 100)
        @Temporal(javax.persistence.TemporalType.DATE)
	private Date fecha;
        
	@Column(name = "fecha_compromiso", nullable = true, length = 100)
        @Temporal(javax.persistence.TemporalType.DATE)
	private Date fecha_compromiso;

        @Column(name ="email_responsable", nullable = false, length = 100)
        private String email_responsable;
        
        @Column (name = "metrosLineales", nullable = false, length = 20)
        private Double metrosLineales;
        
        @Column (name = "totalPaneles", nullable = false, length = 20)
        private Integer totalPaneles;
        
        @Column (name = "tipoProyecto", nullable = false, length = 20)
        private String tipoProyecto;
        
        @Column (name = "tipoProduccion", nullable = false, length = 20)
        private String tipoProduccion;
        
        @Column (name = "version", nullable = false, length = 5)
        private Integer version;
        
        @Column(name ="nombre_cliente", nullable = false, length = 100)
        private String nombre_cliente;
    
        @Column(name ="email_gestor", nullable = false, length = 100)
        private String email_gestor;
        
        @Column(name ="ral", nullable = false, length = 20)
        private String ral;
        
        @Column(name ="pais", nullable = false, length = 5)
        private String pais;
        
        @Column (name = "idCielo", nullable = false, length = 5)
        private Integer idCielo;
        
        @Column(name = "disenyado", nullable = false, length = 10)
	private Boolean disenyado;
        
        // Si el campo puede ser nulo, no hace falta enviarlo desde appglass ya que el json lo rellenara vacio
        @Column(name = "fecha_corte", length = 100)
        @Temporal(javax.persistence.TemporalType.DATE)
	private Date fecha_corte;
        
        @Column (name = "tipo_producto", nullable = false, length = 20)
        private String tipo_producto;
        
        @Column (name = "otros_lacables", nullable = true, length = 512)
        private String otros_lacables;
        
        
        
        /*
        @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        @JoinColumn(name = "PROYECTO_ID")
        @Fetch(FetchMode.SUBSELECT)
        private List<Actividades> listaActividades;
        */
        
        @Transient
        @OneToMany(mappedBy = "proyecto")
        private List<Actividades> actividades;
        
        @Transient
        @OneToMany(mappedBy = "barra")
        private List<Barras> barras;
        
        
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Date getFecha_compromiso() {
        return fecha_compromiso;
    }

    public void setFecha_compromiso(Date fecha_compromiso) {
        this.fecha_compromiso = fecha_compromiso;
    }

    public List<Actividades> getActividades() {
        return actividades;
    }

    public void setActividades(List<Actividades> actividades) {
        this.actividades = actividades;
    }

    public String getEmail_responsable() {
        return email_responsable;
    }

    public void setEmail_responsable(String email_responsable) {
        this.email_responsable = email_responsable;
    }

    public Double getMetrosLineales() {
        return metrosLineales;
    }

    public void setMetrosLineales(Double metrosLineales) {
        this.metrosLineales = metrosLineales;
    }

    public Integer getTotalPaneles() {
        return totalPaneles;
    }

    public void setTotalPaneles(Integer totalPaneles) {
        this.totalPaneles = totalPaneles;
    }

    public String getTipoProyecto() {
        return tipoProyecto;
    }

    public void setTipoProyecto(String tipoProyecto) {
        this.tipoProyecto = tipoProyecto;
    }

    public List<Barras> getBarras() {
        return barras;
    }

    public void setBarras(List<Barras> barras) {
        this.barras = barras;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Date getFecha_corte() {
        return fecha_corte;
    }

    public void setFecha_corte(Date fecha_corte) {
        this.fecha_corte = fecha_corte;
    }

    public String getTipoProduccion() {
        return tipoProduccion;
    }

    public void setTipoProduccion(String tipoProduccion) {
        this.tipoProduccion = tipoProduccion;
    }

    public String getNombre_cliente() {
        return nombre_cliente;
    }

    public void setNombre_cliente(String nombre_cliente) {
        this.nombre_cliente = nombre_cliente;
    }

    public String getEmail_gestor() {
        return email_gestor;
    }

    public void setEmail_gestor(String email_gestor) {
        this.email_gestor = email_gestor;
    }

    public String getRal() {
        return ral;
    }

    public void setRal(String ral) {
        this.ral = ral;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public Integer getIdCielo() {
        return idCielo;
    }

    public void setIdCielo(Integer idCielo) {
        this.idCielo = idCielo;
    }

    public Boolean getDisenyado() {
        return disenyado;
    }

    public void setDisenyado(Boolean disenyado) {
        this.disenyado = disenyado;
    }

    public String getTipo_producto() {
        return tipo_producto;
    }

    public void setTipo_producto(String tipo_producto) {
        this.tipo_producto = tipo_producto;
    }

    public String getOtros_lacables() {
        return otros_lacables;
    }

    public void setOtros_lacables(String otros_lacables) {
        this.otros_lacables = otros_lacables;
    }

}
