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

import java.util.logging.Level;
import java.util.logging.Logger;
import mulan.data.InvalidDataFormatException;
import mulan.data.LabelsMetaData;
import mulan.data.MultiLabelInstances;
import net.sf.jclal.core.IDataset;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Class that represents a Mulan dataset
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 */
public class MulanDataset extends AbstractDataset {

    private MultiLabelInstances multiLabelDataset;

    /**
     * Constructs a empty MulanDataset
     *
     */
    public MulanDataset(){
    }
    
    /**
     * Constructs a MulanDataset
     *
     * @param arffFilePath The path to the arff file
     * @param xmlPath The path to the xml file
     */
    public MulanDataset(String arffFilePath, String xmlPath) {
        try {
            multiLabelDataset = new MultiLabelInstances(arffFilePath, xmlPath);
        } catch (InvalidDataFormatException ex) {
            Logger.getLogger(MulanDataset.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Creates a Mulan Dataset from an MultiLabelInstances object
     *
     * @param dataset The dataset to use
     */
    public MulanDataset(MultiLabelInstances dataset) {

        multiLabelDataset = dataset.clone();

    }

    /**
     * Creates a Mulan Dataset from other Mulan Dataset.
     *
     * @param dataset The dataset to use.
     */
    public MulanDataset(IDataset dataset) {
        multiLabelDataset = ((MulanDataset) dataset).getMultiLabelDataset().clone();
    }

    /**
     * Creates a Mulan Dataset from an Instance object and the corresponding
     * labelsMetaData
     *
     * @param dataset The Instances object
     * @param labelsMetaData The LabelsMetaData object
     */
    public MulanDataset(Instances dataset, LabelsMetaData labelsMetaData) {
        try {
            multiLabelDataset = new MultiLabelInstances(new Instances(dataset), labelsMetaData);
        } catch (InvalidDataFormatException ex) {
            Logger.getLogger(MulanDataset.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Creates a Mulan Dataset from a portion of the MultiLabelInstances object
     *
     * @param dataset The dataset
     * @param first The position of the first instance to copy
     * @param toCopy The number of instances to copy
     */
    public MulanDataset(IDataset dataset, int first, int toCopy) {
        try {
            Instances instances = new Instances(dataset.getDataset(), first, toCopy);
            multiLabelDataset = new MultiLabelInstances(instances, ((MulanDataset) dataset).getLabelsMetaData());
        } catch (InvalidDataFormatException ex) {
            Logger.getLogger(MulanDataset.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public int getNumAttributes() {
        return multiLabelDataset.getDataSet().numAttributes();
    }

    @Override
    public IDataset copy() {
        return new MulanDataset(multiLabelDataset);
    }

    @Override
    public void addAll(IDataset dataset) {
        multiLabelDataset.getDataSet().addAll(dataset.getDataset());
    }

    @Override
    public int getNumInstances() {
        return multiLabelDataset.getNumInstances();
    }

    /**
     * @return The number of labels
     */
    public int getNumLabels() {
        return multiLabelDataset.getNumLabels();
    }

    @Override
    public Instance instance(int index) {

        return multiLabelDataset.getDataSet().instance(index);
    }

    @Override
    public void set(int index, Instance instance) {

        multiLabelDataset.getDataSet().set(index, instance);
    }

    @Override
    public Instances getDataset() {

        return multiLabelDataset.getDataSet();
    }

    public void setDataset(MultiLabelInstances dataset) {

        multiLabelDataset= dataset;
    }

    public MultiLabelInstances getMultiLabelDataset() {

        return multiLabelDataset;
    }

    public LabelsMetaData getLabelsMetaData() {
        return multiLabelDataset.getLabelsMetaData();
    }

    @Override
    public void add(Instance instance) {
        multiLabelDataset.getDataSet().add(instance);
    }

    @Override
    public void remove(int index) {
        multiLabelDataset.getDataSet().remove(index);
    }

    public int[] getLabelIndexes() {
        return multiLabelDataset.getLabelIndices();
    }
}