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
package net.sf.jclal.activelearning.oracle;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
import net.sf.jclal.activelearning.multilabel.querystrategy.AbstractMultiLabelQueryStrategy;
import net.sf.jclal.activelearning.querystrategy.AbstractQueryStrategy;
import net.sf.jclal.activelearning.singlelabel.querystrategy.AbstractSingleLabelQueryStrategy;
import net.sf.jclal.core.IQueryStrategy;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * This class represents a human oracle that is queried by a console.
 *
 * For each selected instances the oracle is queried.
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 */
public class ConsoleHumanOracle extends AbstractOracle {

    /**
     * Labels the instances selected by the query strategy.
     *
     * @param queryStrategy The query strategy to use
     */
    @Override
    public void labelInstances(IQueryStrategy queryStrategy) {

        if (queryStrategy instanceof AbstractMultiLabelQueryStrategy) {
            labelMultiLabelInstances(queryStrategy);
        } else if (queryStrategy instanceof AbstractSingleLabelQueryStrategy) {
            labelSingleLabelInstances(queryStrategy);
        }

    }

    /**
     * Extracts the classes from single-label dataset.
     *
     * @param labeled The labeled set
     * @return An array of string
     */
    private String[] valueClasses(Instances labeled) {

        int classIndex = labeled.classIndex();
        String[] valueClasses = new String[labeled.attribute(classIndex).numValues()];

        for (int i = 0; i < valueClasses.length; i++) {
            valueClasses[i] = labeled.attribute(classIndex).value(i);
        }

        return valueClasses;
    }

    /**
     * Method for the specific case of a multi-label dataset.
     *
     * @param queryStrategy The query strategy to use
     */
    private void labelMultiLabelInstances(IQueryStrategy queryStrategy) {

        //Object to read from the console
        Scanner scanner = new Scanner(new BufferedInputStream(System.in));

        AbstractMultiLabelQueryStrategy multiLabelQueryStrategy = (AbstractMultiLabelQueryStrategy) queryStrategy;

        ArrayList<Integer> selected = multiLabelQueryStrategy.getSelectedInstances();

        ArrayList<String> labels = new ArrayList<String>(multiLabelQueryStrategy.getLabelsMetaData().getLabelNames());

        //For each selected instance
        for (int i : selected) {

            //Ask to the oracle about the class of the instance
            Instance instance = multiLabelQueryStrategy.getUnlabelledData().instance(i);

            System.out.println("\nWhat are the labels of this multi-label instance?");

            System.out.println("Instance:" + instance.toString() + "\n");

            StringTokenizer line;

            do {

                System.out.println("IndexLabels: LabelName");

                int index = 0;

                for (String label : labels) {

                    System.out.println((index++) + "-" + label);
                }

                System.out.println("\n Type the indexes of the labels that the instance belongs separated by a colon");

                line = new StringTokenizer(scanner.next(), ",");

            } while (line.countTokens() == 0);

            //Reset the labels
            for (int labelIndex = 0; labelIndex < multiLabelQueryStrategy.getNumLabels(); labelIndex++) {
                instance.setValue(multiLabelQueryStrategy.getLabelIndices()[labelIndex], 0);
            }

            while (line.hasMoreTokens()) {

                int labelIndex = Integer.valueOf(line.nextToken());

                Attribute att = instance.dataset().attribute(labels.get(labelIndex));

                instance.setValue(att, 1);

            }
            System.out.println();
        }

    }

    /**
     * Method for the specific case of a single-label dataset.
     *
     * @param queryStrategy The query strategy to use
     */
    private void labelSingleLabelInstances(IQueryStrategy queryStrategy) {

        //Object to read from the console
        Scanner scanner = new Scanner(new BufferedInputStream(System.in));

        ArrayList<Integer> selected = ((AbstractQueryStrategy) queryStrategy).getSelectedInstances();

        //In the labeled dataset must be defined all the possible classes
        Instances labeled = queryStrategy.getLabelledData().getDataset();

        String[] valueClass = valueClasses(labeled);

        //For each selected instance
        for (int i : selected) {

            //Ask to the oracle about the class of the instance
            Instance instance = queryStrategy.getUnlabelledData().instance(i);

            System.out.println("\n What is the class of this instance?");

            System.out.println("Instance:" + instance.toString() + "\n");

            int classSelected = 0;

            do {

                System.out.println("Type a number in the range. There are the possible classes:");

                for (int index = 0; index < valueClass.length; index++) {
                    System.out.println(index + ": " + valueClass[index]);
                }

                classSelected = scanner.nextInt();

            } while (classSelected >= valueClass.length || classSelected < 0);

            instance.setClassValue(classSelected);
            System.out.println();
        }

    }

}
