/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.multipart;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>Custom <code>List<BodyPart></code> implementation that maintains
 * parentage information automatically.</p>
 */
class BodyPartsList extends ArrayList<BodyPart> {

    BodyPartsList(MultiPart parent) {
        this.parent = parent;
    }

    MultiPart parent = null;

    @Override
    public boolean add(BodyPart bp) {
        super.add(bp);
        bp.setParent(parent);
        return true;
    }

    @Override
    public void add(int index, BodyPart bp) {
        super.add(index, bp);
        bp.setParent(parent);
    }

    @Override
    public boolean addAll(Collection<? extends BodyPart> bps) {
        if (super.addAll(bps)) {
            for (BodyPart bp : bps) {
                bp.setParent(parent);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends BodyPart> bps) {
        if (super.addAll(index, bps)) {
            for (BodyPart bp : bps) {
                bp.setParent(parent);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void clear() {
        BodyPart bps[] = super.toArray(new BodyPart[super.size()]);
        super.clear();
        for (BodyPart bp : bps) {
            bp.setParent(null);
        }
    }

    @Override
    public BodyPart remove(int index) {
        BodyPart bp = super.remove(index);
        if (bp != null) {
            bp.setParent(null);
        }
        return bp;
    }

    @Override
    public boolean remove(Object bp) {
        if (super.remove(bp)) {
            ((BodyPart) bp).setParent(null);
            return true;
        } else {
            return false;
        }
    }

}
