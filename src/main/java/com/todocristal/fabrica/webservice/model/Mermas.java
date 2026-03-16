package com.todocristal.fabrica.webservice.model;

import java.io.Serializable;


import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;



@Entity(name= "ForeignKeyAssoMermasEntity")
@Table(name = "mermas", uniqueConstraints = {@UniqueConstraint(columnNames ="id")})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

public class Mermas implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "longitud", nullable = false)
    private Integer longitud;

    @Column(name = "color")
    private String color;
    
    @Column(name = "consumida")
    private Boolean consumida;
    
    @Column(name = "hipotecada")
    private Boolean hipotecada;

    @ManyToOne
    private Perfiles perfil;    

    @ManyToOne                
    private Barras barras;


    public long getId() {
            return id;
    }

    public void setId(long id) {
            this.id = id;
    }

    public Integer getLongitud() {
        return longitud;
    }

    public void setLongitud(Integer longitud) {
        this.longitud = longitud;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Perfiles getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfiles perfil) {
        this.perfil = perfil;
    }

    public Barras getBarras() {
        return barras;
    }

    public void setBarras(Barras barras) {
        this.barras = barras;
    }

    public Boolean getConsumida() {
        return consumida;
    }

    public void setConsumida(Boolean consumida) {
        this.consumida = consumida;
    }

    public Boolean getHipotecada() {
        return hipotecada;
    }

    public void setHipotecada(Boolean hipotecada) {
        this.hipotecada = hipotecada;
    }
    
}
