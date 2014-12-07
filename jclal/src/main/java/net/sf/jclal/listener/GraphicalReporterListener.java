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
package net.sf.jclal.listener;

import java.util.ArrayList;
import net.sf.jclal.activelearning.algorithm.ClassicalALAlgorithm;
import net.sf.jclal.core.AlgorithmEvent;
import net.sf.jclal.core.IConfigure;
import net.sf.jclal.core.IEvaluation;
import net.sf.jclal.evaluation.measure.AbstractEvaluation;
import net.sf.jclal.gui.view.components.chart.ExternalBasicChart;
import org.apache.commons.configuration.Configuration;
import org.jfree.ui.RefineryUtilities;

/**
 * This class is a listener for a graphical reporter
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 */
public class GraphicalReporterListener extends ClassicalReporterListener
        implements IConfigure {

    private static final long serialVersionUID = -6866004037911080430L;

    /**
     * It determines if there appears or not the window of visualization of the
     * results
     */
    private boolean showSeparateWindow = false;

    /**
     * Class used to chart
     */
    private ExternalBasicChart externalChart;

    /**
     * Utilitarian variable to complete requisites of the program
     */
    private boolean firstTime = true;

    /**
     * It determines if there appears learning supervised along with active
     * learning, serves to establish visual comparisons
     */
    private boolean showPassiveLearning = false;

    /**
     *
     * @param configuration The configuration object for the Graphical Reporter
     * Listener.
     *
     *The XML labels supported are:
     *
     * <ul>
     * <li><b>show-window= boolean</b></li>
     * <li><b>show-passive-learning= boolean</b></li>
     * </ul>
     */
    @Override
    public void configure(Configuration configuration) {

        super.configure(configuration);

        // By default is false
        boolean showSeparateWindowTemp = configuration.getBoolean("show-window",
                showSeparateWindow);

        setShowSeparateWindow(showSeparateWindowTemp);

        // By default is false
        boolean showPassiveLearningTemp = configuration.getBoolean(
                "show-passive-learning", showPassiveLearning);

        setShowPassiveLearning(showPassiveLearningTemp);
    }

    /**
     * Is executed after the algorithm begins, prepares the conditions of the
     * report: in case of requesting report in file, it creates and keeps the
     * basic configuration of the experiment, show visual report.
     *
     * @param event The event over the algorithm
     */
    @Override
    public void algorithmStarted(AlgorithmEvent event) {

        super.algorithmStarted(event);

        ClassicalALAlgorithm algorithm = (ClassicalALAlgorithm) event
                .getAlgorithm();

        String algorithmName = algorithm.getClass().getSimpleName();

        if (showSeparateWindow) {

            externalChart = new ExternalBasicChart("Active learning process",
                    getReportTitle(), "Number of instances");

            externalChart.setDisabledComponents();

            externalChart.pack();

            this.externalChart.addSerie(new ArrayList<AbstractEvaluation>(), algorithm.getScenario().getQueryStrategy().toString());

            RefineryUtilities.centerFrameOnScreen(externalChart);

            externalChart.setVisible(true);

            // The user wants to show the passive learning in the chart
            if (showPassiveLearning) {
                IEvaluation evaluation = algorithm
                        .getPassiveLearningEvaluation();
                externalChart.setPassiveEvaluation(evaluation);
            }
        }

    }

    /**
     * Is executed whenever the algorithm completes an iteration of active
     * learning, in this case the evaluation of the above mentioned iteration is
     * had, it is possible to store in file or to show it for console or show in
     * a window.
     *
     * @param event The event over the algorithm
     */
    @Override
    public void iterationCompleted(AlgorithmEvent event) {

        super.iterationCompleted(event);

        doIterationVisualReport((ClassicalALAlgorithm) event.getAlgorithm());

    }

    /**
     * It is executed when the experiment finishes, counts with the date of
     * completion of the algorithm.
     *
     * @param event The event over the algorithm
     */
    @Override
    public void algorithmFinished(AlgorithmEvent event) {

        super.algorithmFinished(event);

        doIterationVisualReport((ClassicalALAlgorithm) event.getAlgorithm());

        if (showSeparateWindow) {

            externalChart.enabledMetrics();
        }

    }

    /**
     *
     * @param algorithm The algorithm used.
     */
    protected void doIterationVisualReport(ClassicalALAlgorithm algorithm) {

        int iteration = algorithm.getIteration();

        // Check if this is correct iteration
        if (iteration % getReportFrequency() != 0) {
            return;
        }

        if (showSeparateWindow) {

            // Add the last evaluation
            AbstractEvaluation evaluation = algorithm.getScenario()
                    .getQueryStrategy().getEvaluations().get(iteration - 1);

            if (firstTime) {
                this.externalChart.setMeasuresNames(evaluation.getMetricNames());
                firstTime = false;
            }

            externalChart.add(evaluation);
        }
    }

    /**
     *
     * @return If the windows is showing in separated windows.
     */
    public boolean isShowSeparateWindow() {
        return showSeparateWindow;
    }

    /**
     *
     * @param showSeparateWindow Set if the windows is showing in separated
     * windows.
     */
    public void setShowSeparateWindow(boolean showSeparateWindow) {
        this.showSeparateWindow = showSeparateWindow;
    }

    /**
     *
     * @return If the graphic shows passive learning process.
     */
    public boolean isShowPassiveLearning() {
        return showPassiveLearning;
    }

    /**
     *
     * @param showPassiveLearning Set if the graphic shows passive learning
     * algorithm.
     */
    public void setShowPassiveLearning(boolean showPassiveLearning) {
        this.showPassiveLearning = showPassiveLearning;
    }
}
