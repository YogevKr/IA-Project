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

import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jclal.activelearning.singlelabel.querystrategy.VoteEntropyQueryStrategy;
import net.sf.jclal.core.IConfigure;
import net.sf.jclal.core.IDataset;
import net.sf.jclal.evaluation.measure.AbstractEvaluation;
import net.sf.jclal.evaluation.measure.SingleLabelEvaluation;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationRuntimeException;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

/**
 *
 * Classifier of Weka type for committee.
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 */
public class WekaComitteClassifier extends AbstractClassifier {

    private static final long serialVersionUID = 6176522182059332931L;

    /**
     * weka classifier.
     */
    private Classifier[] classifiers;

    /**
     * Empty(default) constructor.
     */
    public WekaComitteClassifier() {

        super();

    }

    /**
     * Constructs the learning model from the dataset.
     *
     * @param instances The instances to use
     * @throws Exception The exception that will be launched.
     */
    @Override
    public void buildClassifier(IDataset instances) throws Exception {

        for (Classifier classifier : classifiers) {
            classifier.buildClassifier(instances.getDataset());
        }
    }

    /**
     * Classify the instance
     * 
     * @param instance The instance to classify by the committee.
     * @return The predicted label by the comitte.
     * @throws Exception The exception that will be launched
     */
    public double classifyInstance(Instance instance) throws Exception {

        double[] probabilities = distributionForInstance(instance);

        return Utils.maxIndex(probabilities);
    }

    /**
     * Counts the votes of the committee for each class
     *
     * @param instance The instance to test.
     * @return The votes of the committee for each class.
     * @throws Exception The exception that will be launched
     * 
     */
    public int[] countVotesForEachClass(Instance instance) throws Exception {

        int[] votes = new int[instance.dataset().numDistinctValues(instance
                .classIndex())];

        int sizeCommittee = classifiers.length;

        for (int i = 0; i < sizeCommittee; i++) {

            double v = classifiers[i].classifyInstance(instance);

            ++votes[(int) v];

        }

        return votes;
    }

    /**
     * Returns the probability that has the instance to belong to each class.
     *
     * @param instance The instance to test
     * @return The probabilities for each class
     */
    @Override
    public double[] distributionForInstance(Instance instance) {
        try {

            double[] consensus = new double[instance.dataset()
                    .numDistinctValues(instance.classIndex())];

            double sizeCommittee = classifiers.length;

            for (int i = 0; i < sizeCommittee; i++) {

                double[] currentProb = classifiers[i]
                        .distributionForInstance(instance);

                for (int j = 0; j < consensus.length; j++) {
                    consensus[j] += currentProb[j];
                }
            }

            for (int i = 0; i < consensus.length; i++) {
                consensus[i] /= sizeCommittee;
            }

            return consensus;

        } catch (Exception ex) {

            Logger.getLogger(VoteEntropyQueryStrategy.class.getName()).log(
                    Level.SEVERE, null, ex);
        }

        return null;

    }

    /**
     * Evaluates the classifier using the test dataset
     *
     * @param instances The test instances.
     * @return The evaluation of the model.
     */
    @Override
    public AbstractEvaluation testModel(IDataset instances) {

        try {

            // test phase with the actual model
            Evaluation evaluator;

            evaluator = new Evaluation(new Instances(instances.getDataset(), 0));

            Instances testData = instances.getDataset();

            for (Instance in : testData) {

                double temp[] = distributionForInstance(in);

                evaluator.evaluationForSingleInstance(temp, in, true);

            }

            SingleLabelEvaluation sleval = new SingleLabelEvaluation();

            sleval.setEvaluation(evaluator);

            return sleval;

        } catch (Exception e) {
            Logger.getLogger(WekaComitteClassifier.class.getName()).log(
                    Level.SEVERE, null, e);
        }

        return null;
    }

    /**
     * Returns the probability that has the instance to belong to each class according to 
     * an especific classifier of the comitte.
     * @param instance The instance to test.
     * @param indexMember The member of the committee.
     * @return The distribution for instance according to the indexMember
     * classifier.
     */
    public double[] distributionForInstanceByMember(Instance instance,
            int indexMember) {

        try {
            return classifiers[indexMember].distributionForInstance(instance);
        } catch (Exception e) {
            Logger.getLogger(WekaComitteClassifier.class.getName()).log(
                    Level.SEVERE, null, e);
        }
        return null;

    }

    /**
     * The simple names of the classifiers.
     *
     * @return The string of the object.
     */
    @Override
    public String toString() {

        StringBuilder st = new StringBuilder();

        for (Classifier cl : classifiers) {
            st.append(cl.getClass().getSimpleName() + "-");
        }

        st.deleteCharAt(st.length() - 1);

        return st.toString();
    }

    /**
     * Set the comitte of classifiers
     *
     * @param classifiers The classifiers used in the committee.
     */
    public void setClassifiers(Classifier[] classifiers) {

        this.classifiers = new Classifier[classifiers.length];

        int c = 0;

        for (Classifier classifier : classifiers) {
            try {
                this.classifiers[c++] = weka.classifiers.AbstractClassifier
                        .makeCopy(classifier);
            } catch (Exception e) {
                Logger.getLogger(WekaComitteClassifier.class.getName()).log(
                        Level.SEVERE, null, e);
            }
        }

        setNumberClassifiers(classifiers.length);
    }

    /**
     *
     * @param configuration The configuration of Weka committee classifier.
     *The XML labels supported are:
     * <ul>
     * <li>
     * <b>classifier type= class</b>
     * <p>
     * More than one classifier tag can be specified 
     * 
     * Package:
     * weka.classifiers</p>
     * <p>
     * Class: All</p>
     * </li>
     * </ul>
     */
    @Override
    public void configure(Configuration configuration) {

        String classifierError = "classifier type= ";
        try {

            // Number of defined classifiers
            int numberOfClassifiers = configuration
                    .getList("classifier[@type]").size();

            Classifier[] currentClassifiers = new Classifier[numberOfClassifiers];

            // For each classifier in list
            for (int i = 0; i < numberOfClassifiers; i++) {

                String header = "classifier(" + i + ")";

                // classifier classname
                String classifierClassname = configuration.getString(header
                        + "[@type]");
                classifierError += classifierClassname;
                // classifier class
                Class<? extends Classifier> classifierClass = (Class<? extends Classifier>) Class
                        .forName(classifierClassname);

                // classifier instance
                Classifier currentClassifier = classifierClass.newInstance();

                // Configure classifier (if necessary)
                if (currentClassifier instanceof IConfigure) {
                    ((IConfigure) currentClassifier).configure(configuration
                            .subset(header));
                }

                currentClassifiers[i] = currentClassifier;

            }
            // Add this classifier to the strategy
            setClassifiers(currentClassifiers);

        } catch (ClassNotFoundException e) {
            throw new ConfigurationRuntimeException(
                    "\nIllegal classifier classname: " + classifierError, e);
        } catch (InstantiationException e) {
            throw new ConfigurationRuntimeException(
                    "\nIllegal classifier classname: " + classifierError, e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationRuntimeException(
                    "\nIllegal classifier classname: " + classifierError, e);
        }
    }
}
