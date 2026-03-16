/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.todocristal.fabrica.webservice.auxiliar;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author agarcia
 */
public class AuxLogger implements Serializable {
    
    private long id;
    private String nomProyecto;
    private Double longBarraStock;
    private Double longBarrasNecesidad;
    private String color;
    private Integer numBarrasMochilificadas;
    private Double mermaMedia;
    private Double resto;
    private String tipoPerfil;
    private String observaciones;
    

public AuxLogger(String nomProyecto, Double longBarraStock, String auxColor, Double longBarrasNecesidad, Integer numBarrasMochilificadas, Double mermaMedia,  String tipoPerfil){
        this.nomProyecto = nomProyecto;
        this.longBarraStock = longBarraStock;
        this.longBarrasNecesidad = longBarrasNecesidad;
        this.color = auxColor;
        this.numBarrasMochilificadas = numBarrasMochilificadas;
        //this.mermaMedia =(longBarraStock - longBarrasNecesidad) / numBarrasMochilificadas;
        this.mermaMedia = mermaMedia /numBarrasMochilificadas;
        this.resto = 0.0;
        this.tipoPerfil = tipoPerfil;
        this.observaciones = "";
}

@Override
public String toString(){
    return "El proyecto "+ this.nomProyecto + " color :"+this.color+" utiliza barra de stock de :" + this.longBarraStock + ": "+ this.tipoPerfil + ":, para una necesidad INCLUIDAS CTES de :"+ this.longBarrasNecesidad 
            + ":, de :"+ this.numBarrasMochilificadas + ": barras mochilificadas. Merma media :" + this.mermaMedia + ":, resto :" + this.resto + (!this.observaciones.isEmpty()? ":, Obs :" + this.observaciones: "")+"\n";
}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNomProyecto() {
        return nomProyecto;
    }

    public void setNomProyecto(String nomProyecto) {
        this.nomProyecto = nomProyecto;
    }

    public Double getLongBarraStock() {
        return longBarraStock;
    }

    public void setLongBarraStock(Double longBarraStock) {
        this.longBarraStock = longBarraStock;
    }

    public Double getLongBarrasNecesidad() {
        return longBarrasNecesidad;
    }

    public void setLongBarrasNecesidad(Double longBarrasNecesidad) {
        this.longBarrasNecesidad = longBarrasNecesidad;
    }

    public Integer getNumBarrasMochilificadas() {
        return numBarrasMochilificadas;
    }

    public void setNumBarrasMochilificadas(Integer numBarrasMochilificadas) {
        this.numBarrasMochilificadas = numBarrasMochilificadas;
    }

    public Double getMermaMedia() {
        return mermaMedia;
    }

    public void setMermaMedia(Double mermaMedia) {
        this.mermaMedia = mermaMedia;
    }

    public Double getResto() {
        return resto;
    }

    public void setResto(Double resto) {
        this.resto = resto;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTipoPerfil() {
        return tipoPerfil;
    }

    public void setTipoPerfil(String tipoPerfil) {
        this.tipoPerfil = tipoPerfil;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    
    
}
