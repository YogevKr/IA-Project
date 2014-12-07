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
package net.sf.jclal.util.learningcurve;

import java.util.List;
import net.sf.jclal.evaluation.measure.AbstractEvaluation;
import weka.core.Utils;

/**
 * Utility class to handle the learning curve produced by an AL process.
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 */
public class LearningCurveUtility {

    /**
     * Calculates the area under the learning curve (ALC).
     *
     * @param tcurve a list of evaluations
     * @param measureName The measure to use.
     * @return the area under learning curve
     */
    public static double getArea(List<AbstractEvaluation> tcurve, String measureName) {

        final int n = tcurve.size();

        if (n == 0) {
            return Double.NaN;
        }

        //The x-axis represents the number of labeled instances
        final int[] xVals = new int[tcurve.size()];

        //The y-axis represents the values of the metric name
        final double[] yVals = new double[tcurve.size()];

        //fill the xvals and yvals
        for (int i = 0; i < xVals.length; i++) {

            AbstractEvaluation eval = tcurve.get(i);

            xVals[i] = eval.getLabeledSetSize();
            yVals[i] = eval.getMetricValue(measureName);

        }

        double area = 0;
        double xlast = xVals[n - 1];

        double total = 0;

        for (int i = n - 2; i >= 0; i--) {
            double xDelta = Math.abs(xVals[i] - xlast);
            total += xDelta;
            area += (yVals[i] * xDelta);

            xlast = xVals[i];
        }

        if (area == 0) {
            return Utils.missingValue();
        }

        return area/total;
    }
}
