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
package net.sf.jclal.util.sort;

/**
 * It is used to order values and not to lose the index when ordering.
 *
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 *
 * @param <T> The type of object used.
 */
public class Container<T> implements Comparable<Container> {

    /**
     * The element used to order
     */
    private double key;
    /**
     * The value associated to the key
     */
    private T value;

    /**
     *
     * @param key The element used to order
     * @param value The value associated to the key
     */
    public Container(double key, T value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Compare according to the key
     *
     * @param o The element to compare
     * @return The comparison
     */
    @Override
    public int compareTo(Container o) {
        if (key > o.key) {
            return 1;
        }

        if (key < o.key) {
            return -1;
        }
        return 0;
    }

    /**
     *
     * @return The value used to order
     */
    public double getKey() {
        return key;
    }

    /**
     *
     * @return The object
     */
    public T getValue() {
        return value;
    }

    @Override
    public String toString() {

        return "key:" + key + " value:" + value;

    }

    /**
     *
     * @return A copy of the current object
     */
    public Container copy() {
        return new Container(key, value);
    }

    /**
     * Return true if the keys and the values are identical
     *
     * @param container The container
     * @return If the two objects are equals.
     */
    @Override
    public boolean equals(Object container) {

        Container cont = (Container) container;

        return (key == cont.key && value.equals(cont.value));

    }
}
