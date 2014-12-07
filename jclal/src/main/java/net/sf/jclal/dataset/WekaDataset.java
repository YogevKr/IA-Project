/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.jclal.dataset;

import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jclal.core.IDataset;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Class that represents a Weka dataset
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 */
public class WekaDataset extends AbstractDataset {

    private Instances dataset;

    /**
     * Constructs a WekaDataset
     *
     * @param arffFilePath The path to the arff file
     */
    public WekaDataset(String arffFilePath) {
        try {
            dataset = new Instances(new FileReader(arffFilePath));
        } catch (IOException ex) {
            Logger.getLogger(WekaDataset.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Creates a Weka dataset from an Instances object
     *
     * @param dataset The weka dataset
     */
    public WekaDataset(Instances dataset) {
        this.dataset = new Instances(dataset);
    }

    /**
     * Creates a Weka Dataset from other Weka Dataset.
     *
     * @param dataset The dataset to use.
     */
    public WekaDataset(IDataset dataset) {
        this.dataset = new Instances(dataset.getDataset());
    }

    /**
     * Creates a Weka Dataset from a portion of the IDataset object
     *
     * @param dataset The dataset
     * @param first The position of the first instance to copy
     * @param toCopy The number of instances to copy
     */
    public WekaDataset(IDataset dataset, int first, int toCopy) {
        this.dataset = new Instances(dataset.getDataset(), first, toCopy);
    }

    @Override
    public int getNumAttributes() {
        return dataset.numAttributes();
    }

    /**
     * Set the index of the class attribute
     *
     * @param classIndex The index of the class attribute
     */
    public void setClassIndex(int classIndex) {
        dataset.setClassIndex(classIndex);
    }

    @Override
    public IDataset copy() {
        return new WekaDataset(this);
    }

    @Override
    public void addAll(IDataset dataset) {
        this.dataset.addAll(dataset.getDataset());
    }

    @Override
    public int getNumInstances() {
        return dataset.numInstances();
    }

    @Override
    public Instance instance(int index) {
        return dataset.instance(index);
    }

    @Override
    public void set(int index, Instance instance) {
        dataset.set(index, instance);
    }

    public int getNumClasses() {
        return dataset.numClasses();
    }

    public int getClassIndex() {
        return dataset.classIndex();
    }

    @Override
    public void add(Instance instance) {
        dataset.add(instance);
    }

    @Override
    public void remove(int index) {
        dataset.remove(index);
    }

    @Override
    public Instances getDataset() {
        return dataset;
    }
}
