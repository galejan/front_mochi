/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.todocristal.fabrica.webservice.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * @author Rafa
 */
@Entity(name = "ForeignKeyAssoRolesEntity")
@Table(name = "roles", uniqueConstraints = {@UniqueConstraint(columnNames = "id")})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Roles implements Serializable{
    private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "id", unique = true, nullable = false)
	private long id;

	@Column(name = "nombre", length = 100)
	private String nombre;
        
        @Column(name = "descripcion", length = 100)
	private String descripcion;
        
        @Column(name = "codigo", length = 10)
	private String codigo;
        
        /*@ManyToOne
        private Usuarios usuario;
*/
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }   
}
