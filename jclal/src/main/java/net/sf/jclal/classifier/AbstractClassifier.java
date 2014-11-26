/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sf.jclal.classifier;

import java.io.Serializable;
import net.sf.jclal.core.IClassifier;
import net.sf.jclal.core.IConfigure;
import weka.core.SerializedObject;

/**
 * Abstract class used by the classifiers.
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 */
public abstract class AbstractClassifier implements IClassifier, Serializable,
        IConfigure {

    private static final long serialVersionUID = 5403153500223979136L;
    /**
     * Stores the number of classifiers.
     */
    private int numberClassifiers = 1;

    /**
     *
     * @return The number of classifiers used.
     */
    public int getNumberClassifiers() {
        return numberClassifiers;
    }

    /**
     *
     * @param numberClassifiers The number of classifiers used.
     */
    public void setNumberClassifiers(int numberClassifiers) {
        this.numberClassifiers = numberClassifiers;
    }

    /**
     * Copy a classifier.
     *
     * @return a copy of the object
     * @throws Exception The exception that will be launched.
     */
    @Override
    public IClassifier makeCopy() throws Exception {
        return (IClassifier) new SerializedObject(this).getObject();
    }
}
