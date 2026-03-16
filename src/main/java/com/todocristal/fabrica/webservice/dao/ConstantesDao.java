/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.todocristal.fabrica.webservice.dao;

import com.todocristal.fabrica.webservice.model.Constantes;

/**
 *
 * @author rafael
 */
public interface ConstantesDao {
    
    /**
     * Extrae las constantes de la base de datos
     * @return 
     */
    public Constantes cargarConstantes();
}

