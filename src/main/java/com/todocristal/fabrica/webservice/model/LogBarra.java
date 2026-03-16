/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.todocristal.fabrica.webservice.model;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * @author Alex
 */
@Entity(name = "ForeignKeyAssoLogBarrasEntity")
@Table(name = "logbarras", uniqueConstraints = {@UniqueConstraint(columnNames = "id")})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LogBarra implements Serializable{
    private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "id", unique = true, nullable = false)
	private long id;
        
        @Column(name = "perfil", nullable = false )
        private String perfil;
        
        @Column(name = "color", nullable = false )
        private String color;
        
        @Column(name = "longStockBarra", nullable = false )
        private Integer longitud;   
        
        @Column(name = "longMochilificada", nullable = false )
        private Integer longitudMochilificada;  
        
        @Column(name = "unidadesBarrasMochi", nullable = false)
        private Integer unidadesBarrasMochi;
        
        @Column(name = "merma", nullable = false )
        private Integer merma;
        
        @Column(name = "resto", nullable = false )
        private Integer resto;
        
        @Column(name = "obligadaStock", nullable = false )
        private Boolean obligadaStock;
        
        @Column(name = "registro", nullable = false )
	private Timestamp registro;
        
        @Column(name = "proyecto", nullable = false )
	private String proyecto;
        
        @Column (name = "procesado")
        private Timestamp procesado;

    public LogBarra() {
    }

    public LogBarra(String perfil, String color, Integer longitud, Integer longitudMochilificada, Integer unidadesBarrasMochi, Integer merma, Integer resto, Boolean obligadaStock,Timestamp registro, String proyecto) {
        this.perfil = perfil;
        this.color = color;
        this.longitud = longitud;
        this.longitudMochilificada = longitudMochilificada;
        this.unidadesBarrasMochi = unidadesBarrasMochi;
        this.merma = merma;
        this.resto = resto;
        this.obligadaStock = obligadaStock;
        this.registro = registro;
        this.proyecto = proyecto;
    }
    
      public LogBarra(String perfil, String color, Integer longitud, Integer longitudMochilificada, Integer unidadesBarrasMochi, Integer merma, Integer resto, Boolean obligadaStock,Timestamp registro, Timestamp procesado,String proyecto) {
        this.perfil = perfil;
        this.color = color;
        this.longitud = longitud;
        this.longitudMochilificada = longitudMochilificada;
        this.unidadesBarrasMochi = unidadesBarrasMochi;
        this.merma = merma;
        this.resto = resto;
        this.obligadaStock = obligadaStock;
        this.registro = registro;
        this.procesado = procesado;
        this.proyecto = proyecto;
    }
                

   

    public LogBarra(Timestamp registro, String proyecto, Mochilificacion m) {
        this.registro = registro;
        this.perfil = m.getPerfil();
        this.color = m.getStockBarra().getColor();
        this.longitud = m.getStockBarra().getLongitud()!=null ? m.getStockBarra().getLongitud().intValue() : 0;
        this.longitudMochilificada = m.getLongitud()!=null ? m.getLongitud().intValue() : 0;
        this.merma = m.getMerma()!=null ? m.getMerma().intValue() : 0;
        this.resto = m.getResto()!=null ? m.getResto().intValue() : 0;
        this.unidadesBarrasMochi = m.getIndiceBarrasMochilificadas()!=null ? m.getIndiceBarrasMochilificadas().size() : 0;
        this.proyecto = proyecto;
        this.obligadaStock = m.getObligadaStock()!=null ? m.getObligadaStock() : Boolean.FALSE;
    }
        
        
       
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Timestamp getRegistro() {
        return registro;
    }

    public void setRegistro(Timestamp registro) {
        this.registro = registro;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getLongitud() {
        return longitud;
    }

    public void setLongitud(Integer longitud) {
        this.longitud = longitud;
    }

    public Integer getMerma() {
        return merma;
    }

    public void setMerma(Integer merma) {
        this.merma = merma;
    }

    public Integer getResto() {
        return resto;
    }

    public void setResto(Integer resto) {
        this.resto = resto;
    }

    public Integer getLongitudMochilificada() {
        return longitudMochilificada;
    }

    public void setLongitudMochilificada(Integer longitudMochilificada) {
        this.longitudMochilificada = longitudMochilificada;
    }

    public String getProyecto() {
        return proyecto;
    }

    public void setProyecto(String proyecto) {
        this.proyecto = proyecto;
    }

    public Integer getUnidadesBarrasMochi() {
        return unidadesBarrasMochi;
    }

    public void setUnidadesBarrasMochi(Integer unidadesBarrasMochi) {
        this.unidadesBarrasMochi = unidadesBarrasMochi;
    }

    public Timestamp getProcesado() {
        return procesado;
    }

    public void setProcesado(Timestamp procesado) {
        this.procesado = procesado;
    }
   
        
}
