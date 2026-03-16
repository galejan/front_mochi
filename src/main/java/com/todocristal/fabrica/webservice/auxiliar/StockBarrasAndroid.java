/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.todocristal.fabrica.webservice.auxiliar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;




/**
 *
 * @author rafael
 * Se utilizará esta clase para sincronizar la operación del gestor de barras con fábrica.
 */
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StockBarrasAndroid implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Double longitudOrigen;  //Longitud de la barra antes de cortar
    private Double longitudDestino; //Longitud de la barra después de cortar
    private String tipoPerfil;  // Carril, compensador, perfil, etc...       
    private String tipoSistema; // Cortina, corredera, etc...
    private String color;    
    private int unidades;
    
    public StockBarrasAndroid (){
     
    }

    public Double getLongitudOrigen() {
        return longitudOrigen;
    }

    public void setLongitudOrigen(Double longitudOrigen) {
        this.longitudOrigen = longitudOrigen;
    }

    public Double getLongitudDestino() {
        return longitudDestino;
    }

    public void setLongitudDestino(Double longitudDestino) {
        this.longitudDestino = longitudDestino;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getUnidades() {
        return unidades;
    }

    public void setUnidades(int unidades) {
        this.unidades = unidades;
    }
    
    
}