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
package net.sf.jclal.evaluation.method;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jclal.activelearning.algorithm.AbstractALAlgorithm;
import net.sf.jclal.core.IConfigure;
import net.sf.jclal.core.IDataset;
import net.sf.jclal.core.IEvaluationMethod;
import net.sf.jclal.core.ISampling;
import net.sf.jclal.core.ISystem;
import net.sf.jclal.core.ITool;
import net.sf.jclal.dataset.WekaDataset;
import net.sf.jclal.evaluation.measure.AbstractEvaluation;
import net.sf.jclal.util.dataset.DatasetUtils;
import net.sf.jclal.util.random.IRandGen;
import net.sf.jclal.util.random.IRandGenFactory;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationRuntimeException;

/**
 * Abstract class for evaluation methods.
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 */
public abstract class AbstractEvaluationMethod implements IEvaluationMethod,
        IConfigure, ISystem {

    /**
     * Random generator factory.
     */
    protected IRandGenFactory randGenFactory;

    private static final long serialVersionUID = -664807887773087654L;

    /**
     * Pointer to an algorithm.
     */
    private AbstractALAlgorithm algorithm;

    /**
     * Pointer to a full dataset.
     */
    private IDataset dataset;

    /**
     * Path name of the file dataset.
     */
    private String fileDataset;

    /**
     * Pointer to a training dataset.
     */
    private IDataset trainDataset;

    /**
     * Path name of the file train dataset.
     */
    private String fileTrainDataset;

    /**
     * Pointer to a test dataset.
     */
    private IDataset testDataset;

    /**
     * Path name of the file test dataset.
     */
    private String fileTestDataset;

    /**
     * Pointer to a labeled dataset
     */
    private IDataset labeledDataset;

    /**
     * Path name of the file labeled dataset
     */
    private String fileLabeledDataset;

    /**
     * Pointer to a unlabeled dataset
     */
    private IDataset unlabeledDataset;

    /**
     * Path name of the file unlabeled dataset
     */
    private String fileUnlabeledDataset;

    /**
     * Class Attribute index
     */
    private int classAttribute = -1;

    /**
     * Defines the type of learning that will be used, the available types are:
     * single-label, multi-label, multi-instance, multi-instance multi-label,
     * default is single-label
     */
    private int typeOfLearning = 0;

    /**
     * For multi-label dataset
     */
    private String xmlPath;

    /**
     * Evaluations of the classifier
     */
    private List<AbstractEvaluation> finalEvaluations;

    /**
     * It represents the sampling strategy used to select the labeled set
     */
    private ISampling samplingStrategy;

    /**
     *
     * @param algorithm The algorithm used.
     * @param dataset The dataset used.
     */
    public AbstractEvaluationMethod(AbstractALAlgorithm algorithm, IDataset dataset) {

        this.algorithm = algorithm;
        this.dataset = dataset;
    }

    /**
     * Empty(default) constructor.
     */
    public AbstractEvaluationMethod() {
    }

    /**
     * It load the principal elements of the experiment, it uses the methods to load
     * datasets
     */
    public void loadData() {

        try {

            if (randGenFactory != null) {

                if (this.samplingStrategy != null) {
                    this.samplingStrategy.contextualize(this);
                }

                if (algorithm instanceof IConfigure) {
                    ((ITool) algorithm).contextualize(this);
                }
            }

            switch (typeOfLearning) {
                case IEvaluationMethod.SINGLE_LABEL:
                    loadSingleLabelData();
                    break;
                case IEvaluationMethod.MULTI_LABEL:
                    loadMultiLabelData();
                    break;
                case IEvaluationMethod.MULTI_INSTANCE:
                    loadMultiInstanceData();
                    break;
                case IEvaluationMethod.MULTI_INSTANCE_MULTI_LABEL:
                    loadMultiInstanceMultiLabelData();
                    break;
                default:
                    throw new Exception("Type of learning not defined");
            }

        } catch (Exception e) {
            Logger.getLogger(AbstractEvaluationMethod.class.getName()).log(
                    Level.SEVERE, null, e);
        }
    }

    /**
     * Load multi-label dataset.
     */
    public void loadMultiLabelData() {

        if (verifyInitFileDataset(fileDataset)) {

            dataset = DatasetUtils.loadMulanDataSet(fileDataset, xmlPath);

        } // A training and test files were set
        else {

            if (verifyInitFileDataset(fileTestDataset)) {
                testDataset = DatasetUtils.loadMulanDataSet(fileTestDataset, xmlPath);
            }

            if (verifyInitFileDataset(fileTrainDataset)) {

                trainDataset = DatasetUtils.loadMulanDataSet(fileTrainDataset, xmlPath);

            } else {

                //An unlabeled and labeled dataset must be defined
                if (verifyInitFileDataset(fileLabeledDataset)) {
                    labeledDataset = DatasetUtils.loadMulanDataSet(fileLabeledDataset, xmlPath);
                }
                if (verifyInitFileDataset(fileUnlabeledDataset)) {
                    unlabeledDataset = DatasetUtils.loadMulanDataSet(fileUnlabeledDataset, xmlPath);
                }
            }

        }

    }

    /**
     * Load single-label dataset
     */
    public void loadSingleLabelData() {

        //if the user already initialized the datasets
        verifyAndInitWekaDataset((WekaDataset) dataset);
        verifyAndInitWekaDataset((WekaDataset) testDataset);
        verifyAndInitWekaDataset((WekaDataset) trainDataset);
        verifyAndInitWekaDataset((WekaDataset) labeledDataset);
        verifyAndInitWekaDataset((WekaDataset) unlabeledDataset);

        // Only one file dataset is set
        if (verifyInitFileDataset(fileDataset)) {

            dataset = DatasetUtils.loadWekaDataSet(fileDataset);
            classAttribute = foundClassAttribute(dataset);
            ((WekaDataset) dataset).setClassIndex(classAttribute);

        } else {

            if (verifyInitFileDataset(fileTestDataset)) {
                testDataset = DatasetUtils
                        .loadWekaDataSet(fileTestDataset);
                classAttribute = foundClassAttribute(testDataset);
                ((WekaDataset) testDataset).setClassIndex(classAttribute);
            }

            if (verifyInitFileDataset(fileTrainDataset)) {
                trainDataset = DatasetUtils
                        .loadWekaDataSet(fileTrainDataset);
                classAttribute = foundClassAttribute(trainDataset);
                ((WekaDataset) trainDataset).setClassIndex(classAttribute);

            } else {
                //An unlabeled and labeled dataset must be defined
                if (verifyInitFileDataset(fileLabeledDataset)) {
                    labeledDataset = DatasetUtils.loadWekaDataSet(fileLabeledDataset);
                    classAttribute = foundClassAttribute(labeledDataset);
                    ((WekaDataset) labeledDataset).setClassIndex(classAttribute);
                }

                if (verifyInitFileDataset(fileUnlabeledDataset)) {
                    unlabeledDataset = DatasetUtils.loadWekaDataSet(fileUnlabeledDataset);
                    classAttribute = foundClassAttribute(unlabeledDataset);
                    ((WekaDataset) unlabeledDataset).setClassIndex(classAttribute);
                }

            }

        }

        //
    }

    /**
     * Load multi-instance dataset
     */
    public void loadMultiInstanceData() {
        throw new UnsupportedOperationException("The method has not been implemented");
    }

    /**
     * Load multi-instance multi-label dataset
     */
    public void loadMultiInstanceMultiLabelData() {
        throw new UnsupportedOperationException("The method has not been implemented");
    }

    /**
     *
     * @param datasetX The dataset to verify it.
     */
    private void verifyAndInitWekaDataset(WekaDataset datasetX) {
        if (datasetX != null) {
            classAttribute = foundClassAttribute(datasetX);
            datasetX.setClassIndex(classAttribute);
        }
    }

    /**
     *
     * @param fileDatasetX The file dataset that one wants to verify if it has
     * been initialized.
     * @return If the file dataset can be used.
     */
    private boolean verifyInitFileDataset(String fileDatasetX) {
        return fileDatasetX != null && !fileDatasetX.isEmpty();
    }

    /**
     * @return The dataset used.
     */
    public IDataset getDataset() {
        return dataset;
    }

    /**
     *
     * @param dataset The dataset to use.
     */
    public void setDataset(IDataset dataset) {
        this.dataset = dataset;
    }

    /**
     *
     * @return The path file of the dataset.
     */
    public String getFileDataset() {
        return fileDataset;
    }

    /**
     *
     * @param fileDataset The path file of the dataset.
     */
    public void setFileDataset(String fileDataset) {
        this.fileDataset = fileDataset;
    }

    /**
     *
     * @return The instances for train.
     */
    public IDataset getTrainDataset() {
        return trainDataset;
    }

    /**
     *
     * @param trainDataset The instances for train.
     */
    public void setTrainDataset(IDataset trainDataset) {
        this.trainDataset = trainDataset;
    }

    /**
     *
     * @return The path file of the train instances.
     */
    public String getFileTrainDataset() {
        return fileTrainDataset;
    }

    /**
     *
     * @param fileTrainDataset The path file of the train instances
     */
    public void setFileTrainDataset(String fileTrainDataset) {
        this.fileTrainDataset = fileTrainDataset;
    }

    /**
     *
     * @return The test dataset.
     */
    public IDataset getTestDataset() {
        return testDataset;
    }

    /**
     *
     * @param testDataset The test dataset.
     */
    public void setTestDataset(IDataset testDataset) {
        this.testDataset = testDataset;
    }

    /**
     *
     * @return The path file of the test dataset.
     */
    public String getFileTestDataset() {
        return fileTestDataset;
    }

    /**
     *
     * @param fileTestDataset The path file of the test dataset.
     */
    public void setFileTestDataset(String fileTestDataset) {
        this.fileTestDataset = fileTestDataset;
    }

    /**
     *
     * @param labeledDataset The labeled dataset.
     */
    public void setLabeledDataset(IDataset labeledDataset) {
        this.labeledDataset = labeledDataset;
    }

    /**
     *
     * @param unlabeledDataset The unlabeled dataset.
     */
    public void setUnlabeledDataset(IDataset unlabeledDataset) {
        this.unlabeledDataset = unlabeledDataset;
    }

    /**
     *
     * @return The labeled dataset.
     */
    public IDataset getLabeledDataset() {
        return labeledDataset;
    }

    /**
     *
     * @return The unlabeled dataset.
     */
    public IDataset getUnlabeledDataset() {
        return unlabeledDataset;
    }

    /**
     *
     * @return The path file of the labeled dataset.
     */
    public String getFileLabeledDataset() {
        return fileLabeledDataset;
    }

    /**
     *
     * @param fileLabeledDataset The path file of the labeled dataset.
     */
    public void setFileLabeledDataset(String fileLabeledDataset) {
        this.fileLabeledDataset = fileLabeledDataset;
    }

    /**
     *
     * @return The path file of the unlabeled dataset.
     */
    public String getFileUnlabeledDataset() {
        return fileUnlabeledDataset;
    }

    /**
     *
     * @param fileUnlabeledDataset The path file of the unlabeled dataset.
     */
    public void setFileUnlabeledDataset(String fileUnlabeledDataset) {
        this.fileUnlabeledDataset = fileUnlabeledDataset;
    }

    /**
     *
     * @return The algorithm of the experiment.
     */
    @Override
    public AbstractALAlgorithm getAlgorithm() {
        return algorithm;
    }

    /**
     *
     * @param algorithm The algorithm of the experiment.
     */
    @Override
    public void setAlgorithm(AbstractALAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    /**
     *
     * @return The class attribute if the instances are single label.
     */
    public int getClassAttribute() {
        return classAttribute;
    }

    /**
     * Set the class index.
     *
     * @param classAttribute The class attribute if the instances are single
     * label.
     */
    public void setClassAttribute(int classAttribute) {
        this.classAttribute = classAttribute;
    }

    /**
     * Utility method to find the class of a dataset. By default the class is the last attribute.
     *
     * @param data The dataset to use
     * @return The index of the class.
     */
    public int foundClassAttribute(IDataset data) {

        if (data == null) {
            return -1;
        }

        if (classAttribute < 0) {
            classAttribute = data.getNumAttributes() - 1;
        }

        return classAttribute;
    }

    /**
     *
     * @param configuration The configuration object for Abstract Evaluation
     * Method.
     *
     *The XML labels supported are:
     *
     * <ul>
     * <li>
     * <b>rand-gen-factory type= class</b>
     * <p>
     * Package: net.sf.jclal.util.random
     * </p>
     * <p>
     * Class: All
     * </p>
     * </li>
     * <li>
     * <b>sampling-method type= class</b>
     * <p>
     * Package: net.sf.jclal.sampling
     * </p>
     * <p>
     * Class: All
     * </p>
     * </li>
     * <li><b>multi-label= boolean</b></li>
     * <li><b>file-dataset= String</b></li>
     * <li><b>file-train= String</b></li>
     * <li><b>file-test= String</b></li>
     * <li><b>file-labeled= String</b></li>
     * <li><b>file-unlabeled= String</b></li>
     * <li><b>class-attribute= int</b></li>
     * <li>
     * <b>algorithm type= class</b>
     * <p>
     * Package: net.sf.jclal.activelearning
     * </p>
     * <p>
     * Class: All
     * </p>
     * </li>
     * <li><b>file-xml= String</b>: If the dataset is multi label</li>
     * </ul>
     */
    @Override
    public void configure(Configuration configuration) {

        setRandGenSettings(configuration);

        // Set multiLabel flag
        boolean multi = configuration.getBoolean("multi-label", false);
        setMultiLabel(multi);

        // Set the xml file, it is used in the case of a multi-label
        // dataset
        String xml = configuration.getString("file-xml", "");
        setXmlPath(xml);

        //the multi-label elements are verified
        if (multi && xml.isEmpty()) {
            throw new ConfigurationRuntimeException("\nThe multi-label flag is "
                    + "enabled and the xml path is empty. "
                    + "<multi-label>true</multi-label>"
                    + " <file-xml></file-xml>");
        }

        // Set file dataset (default "")
        String fileDatasetT = configuration.getString("file-dataset", "");
        setFileDataset(fileDatasetT);

        if (fileDataset.isEmpty()) {
            // Set file train
            String fileTrain = configuration.getString("file-train", "");
            setFileTrainDataset(fileTrain);
            // Set file test
            String fileTest = configuration.getString("file-test", "");
            setFileTestDataset(fileTest);

            if (fileTest.isEmpty()) {
                throw new ConfigurationRuntimeException("\n If <file-dataset> tag is not specified,"
                        + " then the <file-test> tags must be defined.");
            }

            if (fileTrain.isEmpty()) {

                // Set file labeled
                String fileLabeled = configuration.getString("file-labeled", "");
                setFileLabeledDataset(fileLabeled);

                // Set file labeled
                String fileUnlabeled = configuration.getString("file-unlabeled", "");
                setFileUnlabeledDataset(fileUnlabeled);

                if (fileLabeled.isEmpty() || fileUnlabeled.isEmpty()) {
                    throw new ConfigurationRuntimeException("\n If <file-dataset> and "
                            + " <file-train> tags are not specified,"
                            + " then the <file-labeled> and <file-unlabeled> tags must be defined.");
                }

            }
        }

        // Set class attribute
        int classAttributeT = configuration.getInt("class-attribute", -1);

        setClassAttribute(classAttributeT);

        //Set the configuration of the sampling method
        setSamplingStrategyConfiguration(configuration);

        setAlgorithmSettings(configuration);

    }

    /**
     *
     * @param configuration The configuration of the Algorithm.
     * <p>
     * <b>algorithm type= class</b>
     * </p>
     * <p>
     * Package: net.sf.jclal.activelearning
     * </p>
     * <p>
     * Class: All
     * </p>
     */
    protected void setAlgorithmSettings(Configuration configuration) {

        String algorithmError = "algorithm type= ";
        try {
            // algorithm classname
            String algorithmClassname = configuration
                    .getString("algorithm[@type]");
            algorithmError += algorithmClassname;
            // algorithm class
            Class<? extends AbstractALAlgorithm> algorithmClass = (Class<? extends AbstractALAlgorithm>) Class
                    .forName(algorithmClassname);
            // algorithm instance
            AbstractALAlgorithm algorithm = algorithmClass.newInstance();
            // Configure algorithm (if necessary)
            if (algorithm instanceof IConfigure) {
                ((IConfigure) algorithm).configure(configuration
                        .subset("algorithm"));
            }
            // Add the algorithm
            setAlgorithm(algorithm);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationRuntimeException(
                    "\nIllegal algorithm classname: " + algorithmError, e);
        } catch (InstantiationException e) {
            throw new ConfigurationRuntimeException(
                    "\nIllegal algorithm classname: " + algorithmError, e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationRuntimeException(
                    "\nIllegal algorithm classname: " + algorithmError, e);
        }
    }

    /**
     *
     * @return If the dataset is multi label.
     */
    public boolean isMultiLabel() {
        return typeOfLearning == IEvaluationMethod.MULTI_LABEL;
    }

    /**
     *
     * @param multiLabel Set if the dataset is multi label.
     */
    public void setMultiLabel(boolean multiLabel) {
        if (multiLabel) {
            typeOfLearning = IEvaluationMethod.MULTI_LABEL;
        }
    }

    /**
     *
     * @return The path of the xml file.
     */
    public String getXmlPath() {
        return xmlPath;
    }

    /**
     *
     * @param xmlPath The path of the xml file.
     */
    public void setXmlPath(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    /**
     *
     * @param configuration The configuration of the random generator.
     * <p>
     * rand-gen-factory type= class</p>
     *
     */
    @SuppressWarnings("unchecked")
    protected void setRandGenSettings(Configuration configuration) {
        // Random generators factory
        String randomError = "rand-gen-factory type= ";
        try {
            // Species classname
            String randGenFactoryClassname = configuration
                    .getString("rand-gen-factory[@type]");
            randomError += randGenFactoryClassname;
            // Species class
            Class<? extends IRandGenFactory> randGenFactoryClass = (Class<? extends IRandGenFactory>) Class
                    .forName(randGenFactoryClassname);
            // Species instance
            IRandGenFactory randGenFactory = randGenFactoryClass.newInstance();
            // Configure species
            if (randGenFactory instanceof IConfigure) {
                ((IConfigure) randGenFactory).configure(configuration
                        .subset("rand-gen-factory"));
            }
            // Set species
            setRandGenFactory(randGenFactory);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationRuntimeException(
                    "\nIllegal random generators factory classname: " + randomError);
        } catch (InstantiationException e) {
            throw new ConfigurationRuntimeException(
                    "\nProblems creating an instance of random generators factory: " + randomError,
                    e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationRuntimeException(
                    "\nProblems creating an instance of random generators factory: " + randomError,
                    e);
        }
    }

    /**
     *
     * @return The random generator.
     */
    public IRandGenFactory getRandGenFactory() {
        return randGenFactory;
    }

    /**
     *
     * @param randGenFactory The random generator.
     */
    public void setRandGenFactory(IRandGenFactory randGenFactory) {
        this.randGenFactory = randGenFactory;
    }

    @Override
    public IRandGen createRandGen() {
        return randGenFactory.createRandGen();
    }

    /**
     * @return The sampling strategy
     */
    public ISampling getSamplingStrategy() {
        return samplingStrategy;
    }

    /**
     *
     * @param finalEvaluations Set the final evaluations depending of the
     * evaluation method.
     */
    public void setFinalEvaluations(List<AbstractEvaluation> finalEvaluations) {
        this.finalEvaluations = finalEvaluations;
    }

    /**
     *
     * @return the final evaluations depending of the evaluation method.
     */
    @Override
    public List<AbstractEvaluation> getFinalEvaluations() {
        return finalEvaluations;
    }

    /**
     *
     * @param samplingStrategy The sampling strategy
     */
    public void setSamplingStrategy(ISampling samplingStrategy) {
        this.samplingStrategy = samplingStrategy;
    }

    /**
     * Set the sampling strategy
     *
     * @param configuration The configuration to use
     */
    public void setSamplingStrategyConfiguration(Configuration configuration) {

        String samplingError = "sampling-method type= ";
        try {

            // sampling classname
            String samplingClassname = configuration
                    .getString("sampling-method[@type]");

            samplingError += samplingClassname;
            // sampling class
            Class<? extends ISampling> samplingClass = (Class<? extends ISampling>) Class
                    .forName(samplingClassname);
            // sampling instance
            ISampling sampling = samplingClass.newInstance();
            // Configure sampling (if necessary)
            if (sampling instanceof IConfigure) {
                ((IConfigure) sampling).configure(configuration
                        .subset("sampling-method"));
            }
            // Add this sampling to the algorithm
            setSamplingStrategy(sampling);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationRuntimeException(
                    "\nIllegal ISampling classname: " + samplingError, e);
        } catch (InstantiationException e) {
            throw new ConfigurationRuntimeException(
                    "\nIllegal ISampling classname: " + samplingError, e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationRuntimeException(
                    "\nIllegal ISampling classname: " + samplingError, e);
        }

    }

}
