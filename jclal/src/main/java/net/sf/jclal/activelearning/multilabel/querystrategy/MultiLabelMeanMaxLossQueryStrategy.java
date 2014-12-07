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
package net.sf.jclal.activelearning.multilabel.querystrategy;

import java.util.logging.Level;
import java.util.logging.Logger;
import mulan.transformations.BinaryRelevanceTransformation;
import net.sf.jclal.classifier.SMO;
import net.sf.jclal.classifier.MulanClassifier;
import net.sf.jclal.classifier.ParallelBinaryRelevance;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

/**
 * Implementation of Mean Max Loss active strategy.
 *
 * See for more information Li, X., Wcing, L., and Sung, E. (2004). Multi-label
 * svm active learning for image classification, 2207-2210.
 *
 * @author Oscar Gabriel Reyes Pupo
 *
 */
public class MultiLabelMeanMaxLossQueryStrategy extends
        AbstractMultiLabelQueryStrategy {

    private static final long serialVersionUID = 1L;

    // Array to store the trheshold by classifier
    private double[] threshold;

    /**
     *
     */
    public MultiLabelMeanMaxLossQueryStrategy() {

        super();

    }

    @Override
    public double utilityInstance(Instance instance) {

        if (!(((MulanClassifier) getClassifier()).getInternalClassifier() instanceof ParallelBinaryRelevance)) {
            System.err
                    .println("The Mean Max Loss query strategy must be configured with the Parallel Binary Relevance algorithm");
        }

        ParallelBinaryRelevance learner = (ParallelBinaryRelevance) ((MulanClassifier) getClassifier()).getInternalClassifier();

        // One SVM classiier for each label
        Classifier[] smos = learner.getEnsemble();

        if (!(smos[0] instanceof SMO)) {
            System.err
                    .println("The base classifiers of the Parallel Binary Relevance algorithm on the Mean Max Loss query strategy must be SVM");
        }

        BinaryRelevanceTransformation brt = learner.getBrt();

        int sum = 0;

        int countLabelsPredicted = 0;

        // To predict the labels
        for (int j = 0; j < getNumLabels(); j++) {

            double result;

            try {

                Instance transformedInstance = brt.transformInstance(instance,
                        j);

                result = Math.abs(((SMO) smos[j]).SVMOutput(transformedInstance));

                result = Math.max(1 - instance.value(getLabelIndices()[j])
                        * result, 0);

                // the instance belongs to the j-th label
                if (result <= threshold[j]) {
                    countLabelsPredicted++;

                    for (int l = 0; l < getNumLabels(); l++) {

                        Instance transformedInstance2 = brt.transformInstance(
                                instance, l);

                        result = Math
                                .abs(((SMO) smos[l]).SVMOutput(transformedInstance2));

                        int mjl = -1;

                        if (j == l) {
                            mjl = 1;
                        }

                        sum += Math.max(1 - mjl * result, 0);

                    }

                }

            } catch (Exception e) {

                Logger.getLogger(MultiLabelMeanMaxLossQueryStrategy.class.getName()).log(
                        Level.SEVERE, null, e);
            }

        }

        return sum / countLabelsPredicted;

    }

    @Override
    public void training() {

        super.training();

        ParallelBinaryRelevance learner = (ParallelBinaryRelevance) ((MulanClassifier) getClassifier()).getInternalClassifier();

        Classifier[] smos = learner.getEnsemble();

        BinaryRelevanceTransformation brt = learner.getBrt();

        threshold = new double[getNumLabels()];

        // For each classifier a threshold is computed
        for (int j = 0; j < getNumLabels(); j++) {

            double min = Double.MAX_VALUE;

            // For each instance that belongs to the label
            Instances labeledSet= getLabelledData().getDataset();
            
            for (Instance instance : labeledSet) {

                if (Utils.eq(instance.value(getLabelIndices()[j]), 0.0)) {
                    continue;
                }

                double sum = 0;

                for (int l = 0; l < getNumLabels(); l++) {

                    Instance transformedInstance = brt.transformInstance(
                            instance, l);

                    try {

                        double result = Math.abs(((SMO) smos[l]).SVMOutput(transformedInstance));

                        int mjl = -1;

                        if (j == l) {
                            mjl = 1;
                        }

                        sum += Math.max(1 - mjl * result, 0);

                    } catch (Exception e) {
                        Logger.getLogger(MultiLabelMaxLossQueryStrategy.class.getName()).log(
                                Level.SEVERE, null, e);
                    }
                }

                // The var min stores the smaller value
                if (min > sum) {
                    min = sum;
                }

            }

            threshold[j] = min;

        }
    }
}