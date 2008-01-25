/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.ws.rest.spi.service;

/**
 *
 * @author ps23762
 */
public interface ComponentProvider {
    Object provide(Class c) throws InstantiationException, IllegalAccessException;
}
