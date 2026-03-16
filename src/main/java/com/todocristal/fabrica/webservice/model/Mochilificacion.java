/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.todocristal.fabrica.webservice.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author agarcia
 */
public class Mochilificacion implements Serializable, Comparable<Mochilificacion> {
    
    private ArrayList<Integer> indiceBarrasMochilificadas;
    private Double longitud;
    private Double merma;
    private Double mermaMedia;
    private Double resto;
    private String perfil;
    private Boolean obligadaStock;
    private StockBarras stockBarra;
    private String observaciones;
        
public Mochilificacion (ArrayList<Integer> auxIndiceBarrasMochilificadas, Double auxLongitud, StockBarras auxStockBarra) {
    
     this.indiceBarrasMochilificadas = auxIndiceBarrasMochilificadas;
     this.longitud = auxLongitud;
     this.merma = auxStockBarra.getLongitud() - auxLongitud;
     this.mermaMedia = this.merma / auxIndiceBarrasMochilificadas.size();
     this.perfil = auxStockBarra.getTipoPerfil();
     this.resto = 0.0;
     this.obligadaStock = Boolean.FALSE;
     this.stockBarra = auxStockBarra;
     this.observaciones = "";
     
}

 
public Mochilificacion (Mochilificacion m1, Mochilificacion m2){
    this.indiceBarrasMochilificadas = new ArrayList<Integer>();
    this.indiceBarrasMochilificadas.addAll(m1.getIndiceBarrasMochilificadas());
    this.indiceBarrasMochilificadas.addAll(m2.getIndiceBarrasMochilificadas());
    this.longitud = m1.getLongitud() + m2.getLongitud();
    this.merma = m1.getMerma() + m2.getMerma();
    this.mermaMedia = (m1.getMermaMedia() + m2.getMermaMedia())/2;
    this.perfil = m1.getPerfil();
    this.obligadaStock = Boolean.FALSE;
    Integer longitudBarraCompletaSegunSistema = m1.getStockBarra().getTipoSistema().contains("INFINIA") ? 7000 : 6300;
    this.stockBarra = (m1.getStockBarra().getLongitud().equals(longitudBarraCompletaSegunSistema) ? m1.getStockBarra() : m2.getStockBarra());
    this.resto = 0.0;
    this.observaciones = (m1.getObservaciones() + ";" + m2.getObservaciones() + "; MOCHILIFICACIONES AGRUPADAS");
}
   

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
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

    public StockBarras getStockBarra() {
        return stockBarra;
    }

    public void setStockBarra(StockBarras stockBarra) {
        this.stockBarra = stockBarra;
    }

    public ArrayList<Integer> getIndiceBarrasMochilificadas() {
        return indiceBarrasMochilificadas;
    }

    public void setIndiceBarrasMochilificadas(ArrayList<Integer> indiceBarrasMochilificadas) {
        this.indiceBarrasMochilificadas = indiceBarrasMochilificadas;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public Double getMermaMedia() {
        return mermaMedia;
    }

    public void setMermaMedia(Double mermaMedia) {
        this.mermaMedia = mermaMedia;
    }

    public Boolean getObligadaStock() {
        return obligadaStock;
    }

    public void setObligadaStock(Boolean obligadaStock) {
        this.obligadaStock = obligadaStock;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    @Override
    public int compareTo(Mochilificacion m) {
        
        if (m.getResto() < resto) {
            return -1;
        } else if (m.getResto() > resto) {
            return 1;
        } 
        return 0;
    }
    
    
    @Override
    public String toString(){
        
        return (this.indiceBarrasMochilificadas.toString() + ";" + this.perfil + ";" + String.format("%1$.2f",(this.longitud)) + ";" + String.format("%1$.2f",(this.merma)) + ";" + String.format("%1$.2f",(this.mermaMedia)) 
                + ";" + String.format("%1$.2f",(this.resto))  + ";" + this.stockBarra.getLongitud().toString() + ";" + this.obligadaStock + ";" + this.observaciones +"\n");
    }

}
