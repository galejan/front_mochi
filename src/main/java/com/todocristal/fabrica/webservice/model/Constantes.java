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
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author rafael
 */
@Entity
@Table(name = "constantes")
public class Constantes implements Serializable {
    
    @Id
    @Column(name = "idConstantes")
    @GeneratedValue(strategy=GenerationType.IDENTITY)    
    public Integer id;

    @Column
    private Double MAXMERMA;
     @Column
    private Double MINRESTO;
      @Column
    private Double DESPUNTE;
       @Column
    private Double HOJA;
    
    
}
