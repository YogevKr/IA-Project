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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jclal.activelearning.algorithm.AbstractALAlgorithm;
import net.sf.jclal.activelearning.algorithm.ClassicalALAlgorithm;
import net.sf.jclal.core.IAlgorithm;
import net.sf.jclal.core.IAlgorithmListener;
import net.sf.jclal.core.IDataset;
import net.sf.jclal.dataset.MulanDataset;
import net.sf.jclal.dataset.WekaDataset;
import net.sf.jclal.evaluation.measure.AbstractEvaluation;
import net.sf.jclal.listener.ClassicalReporterListener;
import net.sf.jclal.sampling.AbstractSampling;
import net.sf.jclal.util.dataset.DatasetUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationRuntimeException;

/**
 * K-Fold Cross Validation evaluation method.
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 */
public class kFoldCrossValidation extends AbstractEvaluationMethod {

    private static final long serialVersionUID = 5933144357979580869L;

    /**
     * If the dataset will be stratified
     */
    private boolean stratify;
    /**
     * The number of folds in the cross-validation
     */
    private int numFolds = 10;
    /**
     * Average of the evaluations
     */
    private List<AbstractEvaluation> generalEvaluations;
    /**
     * Average of the supervised evaluations
     */
    private AbstractEvaluation generalSupervisedEvaluation;
    /**
     * Utilitarian var
     */
    private List<Integer> counter;

    /**
     *
     * @param algorithm The algorithm.
     * @param dataset The dataset.
     */
    public kFoldCrossValidation(AbstractALAlgorithm algorithm, IDataset dataset) {
        super(algorithm, dataset);
    }

    /**
     * Empty(default) constructor.
     */
    public kFoldCrossValidation() {
    }

    /**
     * Executes the process of evaluation of the experiment
     */
    @Override
    public void evaluate() {

        try {

            loadData();

            counter = new ArrayList<Integer>();

            if (getDataset() != null) {

                MulanDataset multiLabelDataSet[] = null;

                DatasetUtils.randomize(createRandGen(), getDataset());

                if (stratify) {
                    if (!isMultiLabel()) {
                        DatasetUtils.stratifySingleLabelDataSet(numFolds, (WekaDataset) getDataset());
                    } else {
                        multiLabelDataSet = DatasetUtils.stratifyMultiLabelDataSet(numFolds, (MulanDataset) getDataset());
                    }
                }

                List<AbstractEvaluation> currentFoldEvaluations;

                for (int i = 0; i < numFolds; i++) {

                    if (!isMultiLabel()) {

                        //The list of evaluations is stored
                        currentFoldEvaluations = executeFold(DatasetUtils.trainCV(getDataset(), numFolds, i),
                                DatasetUtils.testCV(getDataset(), numFolds, i), i);

                    } else {
                        //The list of evaluations is stored
                        currentFoldEvaluations = executeFold(DatasetUtils.trainCV(multiLabelDataSet, i), DatasetUtils.testCV(multiLabelDataSet, i), i);

                    }

                    if (generalEvaluations == null) {
                        generalEvaluations = currentFoldEvaluations;
                        fillCounter(generalEvaluations.size());
                    } else {
                        addFoldEvaluations(currentFoldEvaluations);
                    }

                }

                //Average the evaluations over the folds
                averageEvaluations();

                //Simulate the general AL process
                if (!isMultiLabel()) {

                    simulateALProcess(DatasetUtils.trainCV(getDataset(), numFolds, 0),
                            DatasetUtils.testCV(getDataset(), numFolds, 0));

                } else {
                    simulateALProcess(DatasetUtils.trainCV(multiLabelDataSet, 0),
                            DatasetUtils.testCV(multiLabelDataSet, 0));
                }
                
                setFinalEvaluations(generalEvaluations);
            }
        } catch (Exception e) {
            Logger.getLogger(kFoldCrossValidation.class.getName()).log(
                    Level.SEVERE, null, e);
        }
    }

    private List<AbstractEvaluation> executeFold(IDataset trainDataSet, IDataset testDataSet, int fold) {
        try {

            //Resample the instances to construct the labeled and unlabeled set
            getSamplingStrategy().sampling(trainDataSet);

            IAlgorithm algorithmCopy;

            algorithmCopy = getAlgorithm().makeCopy();

            algorithmCopy.setLabeledDataSet(((AbstractSampling) getSamplingStrategy()).getLabeledData());

            algorithmCopy.setUnlabeledDataSet(((AbstractSampling) getSamplingStrategy()).getUnlabeledData());

            algorithmCopy.setTestDataSet(testDataSet);

            //if at least one of the listener extends of ClassicalReporterListener
            ClassicalReporterListener classicalListener = null;

            for (IAlgorithmListener listener : algorithmCopy.getListeners()) {
                if (listener instanceof ClassicalReporterListener) {
                    classicalListener = (ClassicalReporterListener) listener;
                    break;
                }
            }

            if (classicalListener != null) {
                classicalListener.setReportTitle("Fold " + (fold + 1) + "-" + classicalListener.getReportTitle());
            }

            algorithmCopy.execute();

            if (algorithmCopy instanceof ClassicalALAlgorithm) {
                AbstractEvaluation newEvaluation = (AbstractEvaluation) ((ClassicalALAlgorithm) algorithmCopy).getPassiveLearningEvaluation();
                if (generalSupervisedEvaluation == null) {
                    generalSupervisedEvaluation = newEvaluation;
                } else {
                    generalSupervisedEvaluation = addEvaluation(generalSupervisedEvaluation, newEvaluation);
                }

            }

            return algorithmCopy.getScenario().getQueryStrategy().getEvaluations();

        } catch (Exception ex) {
            Logger.getLogger(kFoldCrossValidation.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     *
     * @return If the dataset is stratify.
     */
    public boolean isStratify() {
        return stratify;
    }

    /**
     *
     * @param stratify Set if the dataset is stratify.
     */
    public void setStratify(boolean stratify) {
        this.stratify = stratify;
    }

    /**
     *
     * @return The number of folds used to stratify.
     */
    public int getNumFolds() {
        return numFolds;
    }

    /**
     *
     * @param numFolds The number of folds used to stratify.
     */
    public void setNumFolds(int numFolds) {
        this.numFolds = numFolds;
    }

    /**
     * @param configuration The configuration of K-Fold cross validation.
     *The XML labels supported are:
     *
     * <ul>
     * <li><b>stratify= boolean</b></li>
     * <li><b>num-folds= int</b></li>
     * </ul>
     */
    @Override
    public void configure(Configuration configuration) {
        super.configure(configuration);

        // Set stratify (default false)
        boolean stratifyValue = configuration.getBoolean("stratify", stratify);
        setStratify(stratifyValue);

        // num folds
        int numFols = configuration.getInt("num-folds", numFolds);
        if (numFols < 1) {
            throw new ConfigurationRuntimeException("\n<num-folds>" + numFols + "</num-folds>. "
                    + "num-folds > 0");
        }
        setNumFolds(numFols);
    }

    private void addFoldEvaluations(List<AbstractEvaluation> currentFoldEvaluations) {

        for (int i = 0; i < currentFoldEvaluations.size(); i++) {

            if (i == generalEvaluations.size()) {
                generalEvaluations.add(currentFoldEvaluations.get(i));
            } else {
                generalEvaluations.set(i, addEvaluation(generalEvaluations.get(i), currentFoldEvaluations.get(i)));
            }

            if (i == counter.size()) {
                counter.add(1);
            } else {
                counter.set(i, counter.get(i) + 1);
            }

        }
    }

    private void averageEvaluations() {

        int index = 0;

        //supervised learning counter(the max counter)
        int maxCounter = 1;

        for (AbstractEvaluation evaluation : generalEvaluations) {

            for (String metricName : evaluation.getMetricNames()) {
                evaluation.setMetricValue(metricName, evaluation.getMetricValue(metricName) / counter.get(index));

                //supervised learning
                maxCounter = Math.max(maxCounter, counter.get(index));
            }

            ++index;
        }

        //supervised learning
        if (generalSupervisedEvaluation != null) {
            for (String metricName : generalSupervisedEvaluation.getMetricNames()) {
                generalSupervisedEvaluation.setMetricValue(metricName, generalSupervisedEvaluation.getMetricValue(metricName) / maxCounter);
            }
        }
    }

    /**
     * This method is internally used for a k Fold Cross Validation evaluation
     * method
     *
     * @param evaluationOld the old evaluation
     * @param evaluationNew the new evaluation to add
     * 
     * @return The evaluation
     */
    private AbstractEvaluation addEvaluation(AbstractEvaluation evaluationOld, AbstractEvaluation evaluationNew) {

        for (String metricName : evaluationOld.getMetricNames()) {

            double value1 = evaluationOld.getMetricValue(metricName);
            double value2 = evaluationNew.getMetricValue(metricName);

            value1 = Double.isNaN(value1) ? 0 : value1;
            value2 = Double.isNaN(value2) ? 0 : value2;

            evaluationOld.setMetricValue(metricName, value1 + value2);
        }

        return evaluationOld;
    }

    private void fillCounter(int numOfIterations) {

        counter = new ArrayList<Integer>(numOfIterations);

        for (int i = 0; i < numOfIterations; i++) {
            counter.add(1);
        }
    }

    private void simulateALProcess(IDataset trainDataSet, IDataset testDataSet) {
        try {

            //Resample the instances to construct the labeled and unlabeled set
            getSamplingStrategy().sampling(trainDataSet);

            AbstractALAlgorithm algorithmCopy;

            algorithmCopy = (AbstractALAlgorithm) getAlgorithm().makeCopy();

            algorithmCopy.setLabeledDataSet(((AbstractSampling) getSamplingStrategy()).getLabeledData());

            algorithmCopy.setUnlabeledDataSet(((AbstractSampling) getSamplingStrategy()).getUnlabeledData());

            algorithmCopy.setTestDataSet(testDataSet);

            //if at least one of the listener extends of ClassicalReporterListener
            ClassicalReporterListener classicalListener = null;

            for (IAlgorithmListener listener : algorithmCopy.getListeners()) {
                if (listener instanceof ClassicalReporterListener) {
                    classicalListener = (ClassicalReporterListener) listener;
                    break;
                }
            }

            if (classicalListener != null) {
                classicalListener.setReportTitle("General results-" + classicalListener.getReportTitle());
            }

            algorithmCopy.getScenario().getQueryStrategy().setEvaluations(generalEvaluations);

            if (algorithmCopy instanceof ClassicalALAlgorithm) {
                if (generalSupervisedEvaluation != null) {
                    ((ClassicalALAlgorithm) algorithmCopy).setPassiveLearningEvaluation(generalSupervisedEvaluation);
                }
            }

            algorithmCopy.fireAlgorithmStarted();

            //Simulated the AL process
            for (int i = 1; i < generalEvaluations.size(); i++) {

                ((ClassicalALAlgorithm) algorithmCopy).setIteration(i);

                algorithmCopy.fireIterationCompleted();

            }

            ((ClassicalALAlgorithm) algorithmCopy).setIteration(generalEvaluations.size());
            algorithmCopy.fireAlgorithmFinished();

        } catch (Exception ex) {
            Logger.getLogger(kFoldCrossValidation.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
