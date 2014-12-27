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
package net.sf.jclal.activelearning.singlelabel.querystrategy;

import weka.core.Instance;
import weka.core.Utils;

/**
 * Implementation of Entropy Sampling Strategy (Uncertainty Sampling) query
 * strategy.
 *
 * An uncertainty sampling query strategy that uses entropy as an uncertainty
 * measure
 *
 * Burr Settles. Active Learning Literature Survey. Computer Sciences Technical
 * Report 1648, University ofWisconsin–Madison. 2009.
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Maria del Carmen Rodriguez Hernandez
 * @author Eduardo Perez Perdomo
 *
 */
public class MaximumSurpriseExpectedQueryStrategy extends UncertaintySamplingQueryStrategy {

    private static final long serialVersionUID = 3469267143262432871L;

    /**
     * Manufacturer for defect.
     */
    public MaximumSurpriseExpectedQueryStrategy() {
    }

    /*
     * The surprise query strategy consisting of: S = P(Y) - P(X) where Y is the event with the highest probability.
     */
    @Override
    public double utilityInstance(Instance instance) {

        double[] probabilities = distributionForInstance(instance);

        int indexMax = Utils.maxIndex(probabilities);
        double max = probabilities[indexMax];

        double result = max;

        for (double current : probabilities)
        {
            result = result - current;
        }

        return result < 0? 0 : result;
    }
}