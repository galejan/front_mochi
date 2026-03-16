/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.todocristal.fabrica.webservice.dao;

import com.todocristal.fabrica.webservice.model.Constantes;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author rafael
 */
@Repository
@Transactional
public class ConstantesDaoImpl implements ConstantesDao {

    @Autowired
    private SessionFactory session;

    @Override
    public Constantes cargarConstantes() {
       
        return (Constantes) session.getCurrentSession().get(Constantes.class, 1);
    }

    
}