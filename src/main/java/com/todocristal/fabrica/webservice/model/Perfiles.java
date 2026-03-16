package com.todocristal.fabrica.webservice.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import javax.persistence.Table;

import javax.persistence.UniqueConstraint;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;



@Entity(name= "ForeignKeyAssoPerfilesEntity")
@Table(name = "perfiles", uniqueConstraints = {@UniqueConstraint(columnNames ="id"), @UniqueConstraint(columnNames = "referencia")})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

public class Perfiles implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "referencia", nullable = false)
    private Integer referencia;

    @Column(name = "descripcion")
    private String descripcion;



    public long getId() {
            return id;
    }

    public void setId(long id) {
            this.id = id;
    }

    public Integer getReferencia() {
        return referencia;
    }

    public void setReferencia(Integer referencia) {
        this.referencia = referencia;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    

}
