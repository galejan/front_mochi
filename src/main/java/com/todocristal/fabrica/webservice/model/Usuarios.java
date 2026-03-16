package com.todocristal.fabrica.webservice.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity(name= "ForeignKeyAssoUsuariosEntity")
@Table(name = "usuarios", uniqueConstraints = {@UniqueConstraint(columnNames ="id"), @UniqueConstraint(columnNames = "usuario")})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Usuarios implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "id", nullable = false)
	private long id;

	@Column(name = "usuario", nullable = false)
	private String usuario;
        
        @Column(name = "password")
	private String password;        

	@Column(name = "nombre", length = 100)
	private String nombre;
        
        @Column(name = "activado", length = 10)
	private boolean activado;

        @Column(name = "email", length = 100)
	private String email;
	

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getActivado() {
        return activado;
    }

    public void setActivado(Boolean activado) {
        this.activado = activado;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }   
    
}
