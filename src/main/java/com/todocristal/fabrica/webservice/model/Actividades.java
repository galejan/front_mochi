/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.todocristal.fabrica.webservice.model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * @author Rafa
 */
@Entity(name = "ForeignKeyAssoActividadesEntity")
@Table(name = "actividades", uniqueConstraints = {@UniqueConstraint(columnNames = "id"),@UniqueConstraint(columnNames = {"proyecto_id","codigo"})})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Actividades implements Serializable{
    private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "id", unique = true, nullable = false)
	private long id;

	@Column(name = "codigo", nullable = false, length = 10)
	private Integer codigo;
        
        @Column(name= "nombre", length =100)
        private String nombre;
        
        @Column(name= "peso", length =10)
        private Double peso;
        
        @Column(name= "ordenProduccion", length =10)
        private Double ordenProduccion;
        
        @Column(name= "observaciones", length =255)
        private String observaciones;
        
        @ManyToOne                
        private Proyectos proyecto;
        
        @Transient
        @OneToMany(mappedBy = "actividad")
        private List<Eventos> eventos;
        
        
                
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }
    
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Proyectos getProyecto() {
        return proyecto;
    }

    public void setProyecto(Proyectos proyecto) {
        this.proyecto = proyecto;
    }

    public List<Eventos> getEventos() {
        return eventos;
    }

    public void setEventos(List<Eventos> eventos) {
        this.eventos = eventos;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public Double getOrdenProduccion() {
        return ordenProduccion;
    }

    public void setOrdenProduccion(Double ordenProduccion) {
        this.ordenProduccion = ordenProduccion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

}
