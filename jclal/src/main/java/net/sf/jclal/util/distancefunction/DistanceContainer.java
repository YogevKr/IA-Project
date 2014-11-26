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
package net.sf.jclal.util.distancefunction;

import java.util.ArrayList;
import net.sf.jclal.util.matrixFile.Matrix;
import net.sf.jclal.util.sort.Container;
import net.sf.jclal.util.sort.IndexValueContainer;
import weka.core.Instances;
import weka.core.NormalizableDistance;

/**
 * Implementation of DistanceContainer.
 *
 * Class used to keep the distance between instances
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 *
 */
public class DistanceContainer extends IndexValueContainer {

    /**
     * Similarity matrix
     */
    private double[][] distance;
    /**
     * Similarity matrix stored in file
     */
    private Matrix distanceMatrix;

    /**
     * Number of attributes
     */
    private int numAttributes;

    /**
     * It stores of the matrix used is stored over a file or the main memory
     */
    private boolean matrixOverFile = false;

    /**
     * Similarity matrix
     */
    public double[][] getDistance() {
        return distance;
    }

    /**
     * Similarity matrix
     */
    public void setDistance(double[][] distance) {
        this.distance = distance;
    }

    /**
     * Number of attributes
     */
    public int getNumAttributes() {
        return numAttributes;
    }

    /**
     * Number of attributes
     */
    public void setNumAttributes(int numAttributes) {
        this.numAttributes = numAttributes;
    }

    /**
     * Constructor by default
     *
     * @param instances dataset
     * @param distanceFunction The distance function used to calculate the
     * distance
     */
    public DistanceContainer(Instances instances,
            NormalizableDistance distanceFunction) {

        size = instances.numInstances();

        indexesChanges = new int[size];

        acumulativeValue = new double[size];

        numAttributes = instances.numAttributes();

        int m = size - 1;

        distance = new double[m][];

        int temp;
        double valueTemp;
        for (int i = 0; i < m; ++i) {

            distance[i] = new double[size - i - 1];

            //In the begining the index and the value are equals
            indexesChanges[i] = i;

            for (int j = i + 1; j < size; ++j) {
                temp = j - i - 1;

                valueTemp = distanceFunction.distance(instances.instance(i),
                        instances.instance(j));

                setStoreDistance(i, temp, valueTemp);

                //acumulative distance
                acumulativeValue[i] += valueTemp;
                acumulativeValue[j] += valueTemp;
            }
        }

        indexesChanges[size - 1] = size - 1;
    }

    /**
     * Constructor by default
     *
     * @param instances dataset
     * @param distanceFunction The distance function used to calculate the
     * distance
     */
    public DistanceContainer(Instances instances, NormalizableDistance distanceFunction,
            boolean matrixOverFile) throws Exception {

        this.matrixOverFile = matrixOverFile;

        size = instances.numInstances();

        indexesChanges = new int[size];

        acumulativeValue = new double[size];

        numAttributes = instances.numAttributes();

        int m = size - 1;

        if (matrixOverFile) {
            distanceMatrix = new Matrix(size, size, true);
        } else {
            distance = new double[m][];
        }

        int temp;
        double valueTemp;
        for (int i = 0; i < m; ++i) {

            if (!matrixOverFile) {
                distance[i] = new double[size - i - 1];
            }

            //In the begining the index and the value are equals
            indexesChanges[i] = i;

            for (int j = i + 1; j < size; ++j) {
                temp = j - i - 1;
                valueTemp = distanceFunction.distance(instances.instance(i),
                        instances.instance(j));

                setStoreDistance(i, temp, valueTemp);

                //acumulative distance
                acumulativeValue[i] += valueTemp;
                acumulativeValue[j] += valueTemp;
            }
        }

        indexesChanges[size - 1] = size - 1;
    }

    /**
     * It stores in the distance matrix the value in row and column
     *
     * @param r
     * @param c
     * @param value
     */
    protected void setStoreDistance(int r, int c, double value) {
        if (matrixOverFile) {
            distanceMatrix.set(r, c, value);
        } else {
            distance[r][c] = value;
        }
    }

    /**
     * the distance value of the matrix in row and column
     *
     * @param r
     * @param c
     * @return
     */
    protected double getStoreDistance(int r, int c) {
        if (matrixOverFile) {
            return distanceMatrix.get(r, c);
        } else {
            return distance[r][c];
        }
    }

    /**
     * Returns the value of the distance between x and y, use the method
     * getStoreDistance
     *
     * @param indexX The index of the instance x.
     * @param indexY The index of the instance y.
     * @return Distance value between x and y.
     */
    public double getDistance(int indexX, int indexY) {

        int posX = indexesChanges[indexX];

        int posY = indexesChanges[indexY];

        if (posX == posY) {
            return 0;
        }

        int r = Math.min(posX, posY);
        int c = Math.max(posX, posY);

        double similarityValue = getStoreDistance(r, c - r - 1);

        return similarityValue;
    }

    /**
     * Update the indexes. It is used to accelerate the process.
     *
     * @param removedIndexes Array that contains the indexes that will be
     * removed
     */
    public void updateIndexes(ArrayList<Integer> removedIndexes) {

        ArrayList<Container> ordered = obtainOrdered(removedIndexes);

        for (Container indexToRemove : ordered) {

            updateIndex(Integer.parseInt(indexToRemove.getValue().toString()));

        }

    }

    /**
     * Updates the values of distance on having eliminated the instance of the
     * given index
     *
     * @param index
     */
    public void updateIndex(int index) {
        for (int i = 0; i < index; i++) {
            setAcumulativeValue(i, getAcumulativeValue(i)
                    - getDistance(index, i));
        }

        for (int i = index + 1; i < size; i++) {
            setAcumulativeValue(i, getAcumulativeValue(i)
                    - getDistance(index, i));
        }

        //delete index
        deleteIndex(index);
    }

    /**
     * Free the space of the matrix in memory and harddrive
     */
    public void destroy() {
        if (distanceMatrix != null) {
            distanceMatrix.destroy();
        }

        acumulativeValue = null;
        indexesChanges = null;
        distance = null;
        distanceMatrix = null;
    }
}
