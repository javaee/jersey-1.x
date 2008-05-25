/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

/*
 * Widget.java
 *
 * Created on April 6, 2007, 5:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.jersey.samples.persistence;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Entity class Widget
 * 
 * @author mh124079
 */
@Entity
@Table(name = "WIDGET")
@NamedQueries( {
        @NamedQuery(name = "Widget.findById", query = "SELECT w FROM Widget w WHERE w.id = :id"),
        @NamedQuery(name = "Widget.findByName", query = "SELECT w FROM Widget w WHERE w.name = :name"),
        @NamedQuery(name = "Widget.findByDescription", query = "SELECT w FROM Widget w WHERE w.description = :description")
    })
public class Widget implements Serializable {

    @Id
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;
    
    /** Creates a new instance of Widget */
    public Widget() {
    }

    /**
     * Creates a new instance of Widget with the specified values.
     * @param id the id of the Widget
     */
    public Widget(Integer id) {
        this.id = id;
    }

    /**
     * Creates a new instance of Widget with the specified values.
     * @param id the id of the Widget
     * @param name the name of the Widget
     */
    public Widget(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Gets the id of this Widget.
     * @return the id
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Sets the id of this Widget to the specified value.
     * @param id the new id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the name of this Widget.
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of this Widget to the specified value.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of this Widget.
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the description of this Widget to the specified value.
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns a hash code value for the object.  This implementation computes 
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this Widget.  The result is 
     * <code>true</code> if and only if the argument is not null and is a Widget object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Widget)) {
            return false;
        }
        Widget other = (Widget)object;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "com.example.persistence.Widget[id=" + id + "]";
    }
    
}
