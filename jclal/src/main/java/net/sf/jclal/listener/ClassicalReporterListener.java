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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jclal.activelearning.algorithm.ClassicalALAlgorithm;
import net.sf.jclal.activelearning.batchmode.AbstractBatchMode;
import net.sf.jclal.activelearning.scenario.AbstractScenario;
import net.sf.jclal.core.AlgorithmEvent;
import net.sf.jclal.core.IAlgorithmListener;
import net.sf.jclal.core.IConfigure;
import net.sf.jclal.evaluation.measure.AbstractEvaluation;
import net.sf.jclal.util.file.FileUtil;
import net.sf.jclal.util.mail.SenderEmail;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationRuntimeException;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * This class is a listener for ClassicalALAlgorithm. It performs a report of
 * the actual iteration.
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 */
public class ClassicalReporterListener implements IAlgorithmListener,
        IConfigure {
    // ///////////////////////////////////////////////////////////////
    // --------------------------------------- Serialization constant
    // ///////////////////////////////////////////////////////////////

    private static final long serialVersionUID = -6866004037911080430L;

    // ///////////////////////////////////////////////////////////////
    // --------------------------------------------------- Properties
    // ///////////////////////////////////////////////////////////////
    /**
     * Name of the report
     */
    private String reportTitle = "untitled";

    /**
     * Directory of the report
     */
    private String reportDirectory = "reports/";

    /**
     * Report frequency
     */
    private int reportFrequency = 1;

    /**
     * Show report on console?
     */
    private boolean reportOnConsole = true;

    /**
     * Write report on file?
     */
    private boolean reportOnFile = false;

    // ///////////////////////////////////////////////////////////////
    // ------------------------------------------- Internal variables
    // ///////////////////////////////////////////////////////////////
    /**
     * Report file
     */
    private File reportFile;

    /**
     * It is used to send a email
     */
    private SenderEmail senderEmail;

    /**
     * It is used to send a email
     *
     * @return A SenderEmail object
     *
     */
    public SenderEmail getSenderEmail() {
        return senderEmail;
    }

    /**
     * It is used to send a email
     *
     * @param senderEmail The sender email
     */
    public void setSenderEmail(SenderEmail senderEmail) {
        this.senderEmail = senderEmail;
    }

    // ///////////////////////////////////////////////////////////////
    // ------------------------------------------------- Constructors
    // ///////////////////////////////////////////////////////////////
    /**
     * Empty(default) constructor.
     */
    public ClassicalReporterListener() {
        super();
    }

    // ///////////////////////////////////////////////////////////////
    // ----------------------------------------------- Public methods
    // ///////////////////////////////////////////////////////////////
    // Setting and getting properties
    /**
     *
     * @return The title of the report.
     */
    public final String getReportTitle() {
        return reportTitle;
    }

    /**
     *
     * @param reportTitle The title of the report.
     */
    public final void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    /**
     *
     * @return The frequency of the report.
     */
    public final int getReportFrequency() {
        return reportFrequency;
    }

    /**
     *
     * @param reportFrequency The frequency of the report.
     */
    public final void setReportFrequency(int reportFrequency) {
        this.reportFrequency = reportFrequency;
    }

    /**
     *
     * @return If the report is showing in console.
     */
    public boolean isReportOnConsole() {
        return reportOnConsole;
    }

    /**
     *
     * @param reportOnCconsole Set if the report is showing in console.
     */
    public final void setReportOnConsole(boolean reportOnCconsole) {
        this.reportOnConsole = reportOnCconsole;
    }

    /**
     *
     * @return If the report is saved in a file.
     */
    public final boolean isReportOnFile() {
        return reportOnFile;
    }

    /**
     *
     * @param reportOnFile Set if the report is saved in a file.
     */
    public final void setReportOnFile(boolean reportOnFile) {
        this.reportOnFile = reportOnFile;
    }

    /**
     *
     * @param configuration The configuration of Classical Reporter Listener.
     *
     * The XML labels supported are:
     *
     * <ul>
     * <li><b>report-title= String</b>, default= untitled</li>
     * <li><b>report-directory= String</b>, default= reports</li>
     * <li><b>report-frequency= int</b></li>
     * <li><b>report-on-console= boolean</b></li>
     * <li><b>report-on-file= boolean</b></li>
     * <li><b>send-email= class</b>
     * <p>
     * Package: net.sf.jclal.util.mail</p>
     * Class: All
     * </li>
     * </ul>
     */
    @Override
    public void configure(Configuration configuration) {

        // Set report title (default "untitled")
        String reportTitleT = configuration.getString("report-title", reportTitle);
        setReportTitle(reportTitleT);

        // Set report title (default "reports/")
        String reportDirectoryT = configuration.getString("report-directory",
                reportDirectory);
        setReportDirectory(reportDirectoryT);

        // Set report frequency (default 1 iteration)
        int reportFrequencyT = configuration.getInt("report-frequency", reportFrequency);
        setReportFrequency(reportFrequencyT);

        // Set console report (default on)
        boolean reportOnConsoleT = configuration
                .getBoolean("report-on-console", reportOnConsole);
        setReportOnConsole(reportOnConsoleT);

        // Set file report (default off)
        boolean reportOnFileT = configuration.getBoolean("report-on-file", reportOnFile);
        setReportOnFile(reportOnFileT);

        String sendError = "send-email type= ";
        try {

            String senderEmailClassname
                    = configuration.getString("send-email[@type]");
            sendError += senderEmailClassname;
            //If a email sender was especified
            if (senderEmailClassname != null) {
                // sender email class
                Class<?> senderEmailClass = Class.forName(senderEmailClassname);

                SenderEmail senderEmailT = (SenderEmail) senderEmailClass.newInstance();

                // Configure listener (if necessary)
                if (senderEmailT instanceof IConfigure) {
                    ((IConfigure) senderEmailT).configure(configuration.subset("send-email"));
                }

                setSenderEmail(senderEmailT);
            }

        } catch (ClassNotFoundException e) {
            throw new ConfigurationRuntimeException("\nIllegal sender email classname: "
                    + sendError, e);
        } catch (InstantiationException e) {
            throw new ConfigurationRuntimeException("\nIllegal sender email classname: "
                    + sendError, e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationRuntimeException("\nIllegal sender email classname: "
                    + sendError, e);
        }
    }

    /**
     * Is executed after the algorithm begins, prepares the conditions of the
     * report: in case of requesting report in file, it creates and keeps the
     * basic configuration of the experiment.
     *
     * @param event The event over the algorithm
     */
    @Override
    public void algorithmStarted(AlgorithmEvent event) {

        ClassicalALAlgorithm algorithm = (ClassicalALAlgorithm) event
                .getAlgorithm();

        // Create report title for this instance
        String dateString = new Date(System.currentTimeMillis()).toString()
                .replace(':', '.');

        //new validation
        File directory = new File(reportDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        if (!reportDirectory.endsWith("/")) {
            reportDirectory += "/";
        }

        String actualReportTitle = reportDirectory + reportTitle + " " + dateString;

        //Writing the header of the report file
        StringBuilder st = new StringBuilder(dateString + "\n");
        st.append("Dataset: ").append(algorithm.getScenario().getQueryStrategy().getLabelledData().getDataset()
                .relationName()).append("\n");

        st.append("Test set size: ").append(algorithm.getTestDataSet().getNumInstances()).append("\n");

        st.append("Initial Labelled set size: ").append(algorithm.getScenario().getQueryStrategy().getLabelledData()
                .getNumInstances()).append("\n");

        st.append("Initial Unlabelled set size: ").append(algorithm.getScenario().getQueryStrategy().getUnlabelledData()
                .getNumInstances()).append("\n");

        AbstractBatchMode batchMode = (AbstractBatchMode) ((AbstractScenario) algorithm.getScenario()).getBatchMode();

        st.append("Batch mode: ").append(batchMode).append("\n");

        st.append("Batch size: ").append(batchMode.getBatchSize()).append("\n");

        st.append("Classifiers: ").append(algorithm.getScenario().getQueryStrategy().getClassifier()).append("\n");

        st.append("Scenario: ").append(algorithm.getScenario()).append("\n");

        st.append("Query strategy: ").append(algorithm.getScenario().getQueryStrategy()).append("\n");

        st.append("Time begining: ").append(dateString).append("\n\t\t\n");

        if (reportOnConsole) {
            System.out.println(st.toString());
        }

        // If report is stored in a text file, create report file
        if (reportOnFile) {
            reportFile = new File(actualReportTitle + ".report.txt");
            try {

                FileUtil.writeFile(reportFile, st.toString());

            } catch (IOException e) {
                Logger.getLogger(ClassicalReporterListener.class.getName()).log(
                        Level.SEVERE, null, e);
            }
        }
    }

    /**
     * Is executed whenever the algorithm completes an iteration of active
     * learning, in this case the evaluation of the above mentioned iteration is
     * had, it is possible to store in file or to show it for console.
     *
     * @param event The event over the algorithm
     */
    @Override
    public void iterationCompleted(AlgorithmEvent event) {
        doIterationReport((ClassicalALAlgorithm) event.getAlgorithm());
    }

    /**
     * It is executed when the experiment finishes, counts with the date of
     * completion of the algorithm.
     *
     * @param event The event over the algorithm
     */
    @Override
    public void algorithmFinished(AlgorithmEvent event) {

        // Do last iteration report
        doIterationReport((ClassicalALAlgorithm) event.getAlgorithm());

        String dateString = new Date(System.currentTimeMillis()).toString()
                .replace(':', '.');

        if (reportOnConsole) {
            System.out.println("Time end:" + dateString);
        }

        // Close report file if necessary
        if (reportOnFile && reportFile != null) {
            try {
                
                FileUtil.writeFile(reportFile, "Time end:" + dateString);

            } catch (IOException e) {
                Logger.getLogger(ClassicalReporterListener.class.getName()).log(
                        Level.SEVERE, null, e);
            }
        }

        //An email is sent
        if (senderEmail != null) {
            senderEmail.sendEmail("JCLAL reporter-Algorithm Finished", "Finish Algorithm", reportFile);
        }
    }

    /**
     * It is used after to complete the algorithm, executing any necessary
     * action.
     */
    @Override
    public void algorithmTerminated(AlgorithmEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ClassicalReporterListener) {
            ClassicalReporterListener cother = (ClassicalReporterListener) other;
            EqualsBuilder eb = new EqualsBuilder();
            // reportTitle
            eb.append(reportTitle, cother.reportTitle);
            // reportFrequency
            eb.append(reportFrequency, cother.reportFrequency);
            // reportOnConsole
            eb.append(reportOnConsole, cother.reportOnConsole);
            // reportOnFile
            eb.append(reportOnFile, cother.reportOnFile);

            return eb.isEquals();
        } else {
            return false;
        }
    }

    /**
     *
     * @param algorithm The algorithm used.
     */
    protected void doIterationReport(ClassicalALAlgorithm algorithm) {

        int iteration = algorithm.getIteration();

        // Check if this is correct iteration
        if (iteration % reportFrequency != 0) {
            return;
        }

        // Get the last evaluation
        AbstractEvaluation last = algorithm.getScenario().getQueryStrategy()
                .getEvaluations().get(iteration - 1);

        // Do iteration report
        // Write report string to the standard output (if necessary)
        if (reportOnConsole) {
            System.out.println(last.toString());
        }

        // Write string to the report file (if necessary)
        if (reportOnFile) {
            try {
                
                FileUtil.writeFile(reportFile, last.toString());
                
            } catch (IOException e) {
                Logger.getLogger(ClassicalReporterListener.class.getName()).log(
                        Level.SEVERE, null, e);
            }
        }
    }

    /**
     *
     * @return The file of the report directory.
     */
    public String getReportDirectory() {
        return reportDirectory;
    }

    /**
     *
     * @param reportDirectory The file of the report directory.
     */
    public void setReportDirectory(String reportDirectory) {
        this.reportDirectory = reportDirectory;
    }
}
