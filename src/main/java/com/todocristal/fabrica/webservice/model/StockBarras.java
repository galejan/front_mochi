package com.todocristal.fabrica.webservice.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;



@Entity(name= "ForeignKeyAssoStockBarrasEntity")
@Table(name = "stockBarras", uniqueConstraints = {@UniqueConstraint(columnNames ="id")})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

public class StockBarras implements Serializable, Comparable<StockBarras> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "longitud", nullable = false)
    private Double longitud;  // Longitud de la barra una vez cortada
    
    @Column(name = "tipo_perfil")
    private String tipoPerfil;  // Carril, compensador, perfil, etc...
    
    @Column(name = "tipo_sistema")
    private String tipoSistema; // Cortina, corredera, etc...
    
    
    @Column(name = "color")
    private String color;    // Color de la barra
    
    @Column(name = "unidades")
    private Integer unidades;
    
    @Column(name = "stock_forzar")
    private Integer stockForzar;
    
    @Column(name = "merma_forzar")
    private Double mermaForzar;
    
    public StockBarras(){
        
    }
    
    public StockBarras(String tipoPerfil, String color, Double longitud, String tipoSistema){
        this.tipoPerfil = tipoPerfil;
        this.color = color;
        this.longitud = longitud;
        this.tipoSistema = tipoSistema;
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

    

    public String getTipoSistema() {
        return tipoSistema;
    }

    public void setTipoSistema(String tipoSistema) {
        this.tipoSistema = tipoSistema;
    }

   

    public Integer getUnidades() {
        return unidades;
    }

    public void setUnidades(Integer unidades) {
        this.unidades = unidades;
    }

    public Integer getStockForzar() {
        return stockForzar;
    }

    public void setStockForzar(Integer stockForzar) {
        this.stockForzar = stockForzar;
    }

    public Double getMermaForzar() {
        return mermaForzar;
    }

    public void setMermaForzar(Double mermaForzar) {
        this.mermaForzar = mermaForzar;
    }

    

    @Override
    public int compareTo(StockBarras b) {                
        if(b.getLongitud() < longitud && b.getTipoPerfil().equals(tipoPerfil)){
            return -1;
        }
        if(b.getLongitud() > longitud && b.getTipoPerfil().equals(tipoPerfil)){
            return 1;
        }
        
        return 0;
    }
    
   

    
}
