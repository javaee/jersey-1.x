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

package com.sun.ws.rest.samples.bookmark.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Primary Key class BookmarkEntityPK for entity class BookmarkEntity
 * 
 * @author japod
 */
@Embeddable
public class BookmarkEntityPK implements Serializable {

    @Column(name = "USERID", nullable = false)
    private String userid;

    @Column(name = "BMID", nullable = false)
    private String bmid;
    
    /** Creates a new instance of BookmarkEntityPK */
    public BookmarkEntityPK() {
    }

    /**
     * Creates a new instance of BookmarkEntityPK with the specified values.
     * @param bmid the bmid of the BookmarkEntityPK
     * @param userid the userid of the BookmarkEntityPK
     */
    public BookmarkEntityPK(String bmid, String userid) {
        this.bmid = bmid;
        this.userid = userid;
    }

    /**
     * Gets the userid of this BookmarkEntityPK.
     * @return the userid
     */
    public String getUserid() {
        return this.userid;
    }

    /**
     * Sets the userid of this BookmarkEntityPK to the specified value.
     * @param userid the new userid
     */
    public void setUserid(String userid) {
        this.userid = userid;
    }

    /**
     * Gets the bmid of this BookmarkEntityPK.
     * @return the bmid
     */
    public String getBmid() {
        return this.bmid;
    }

    /**
     * Sets the bmid of this BookmarkEntityPK to the specified value.
     * @param bmid the new bmid
     */
    public void setBmid(String bmid) {
        this.bmid = bmid;
    }

    /**
     * Returns a hash code value for the object.  This implementation computes 
     * a hash code value based on the id fields in this object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.bmid != null ? this.bmid.hashCode() : 0);
        hash += (this.userid != null ? this.userid.hashCode() : 0);
        return hash;
    }

    /**
     * Determines whether another object is equal to this BookmarkEntityPK.  The result is 
     * <code>true</code> if and only if the argument is not null and is a BookmarkEntityPK object that 
     * has the same id field values as this object.
     * @param object the reference object with which to compare
     * @return <code>true</code> if this object is the same as the argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof BookmarkEntityPK)) {
            return false;
        }
        BookmarkEntityPK other = (BookmarkEntityPK)object;
        if (this.bmid != other.bmid && (this.bmid == null || !this.bmid.equals(other.bmid))) return false;
        if (this.userid != other.userid && (this.userid == null || !this.userid.equals(other.userid))) return false;
        return true;
    }

    /**
     * Returns a string representation of the object.  This implementation constructs 
     * that representation based on the id fields.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "com.sun.ws.rest.samples.bookmark.entities.BookmarkEntityPK[bmid=" + bmid + ", userid=" + userid + "]";
    }
    
}
