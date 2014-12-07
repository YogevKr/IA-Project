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
package net.sf.jclal.experiment;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jclal.core.IConfigure;
import net.sf.jclal.core.IEvaluationMethod;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

/**
 * Class to execute a experiment.
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 */
public class Experiment {

    /**
     * The evaluation method used
     */
    private IEvaluationMethod method;
    /**
     * Time of execution of the program in miliseconds
     */
    private long runtime;

    /**
     *
     * @param jobFilename The xml configuration of the experiment.
     */
    @SuppressWarnings("unchecked")
    public void executeJob(String jobFilename) {
        // Try open job file
        File jobFile = new File(jobFilename);
        if (jobFile.exists()) {
            try {
                // Job configuration
                XMLConfiguration jobConf = new XMLConfiguration(jobFile);

                // Process header
                String header = "process";

                // Create and configure evaluation method
                String aname = jobConf.getString(header + "[@evaluation-method-type]");

                Class<IEvaluationMethod> aclass = (Class<IEvaluationMethod>) Class.forName(aname);

                IEvaluationMethod evaluationMethod = aclass.newInstance();

                // Configure runner
                if (evaluationMethod instanceof IConfigure) {
                    ((IConfigure) evaluationMethod).configure(jobConf.subset(header));
                }

                long t1 = System.currentTimeMillis();
                // Execute evaluation runner
                evaluationMethod.evaluate();

                method = evaluationMethod;

                t1 = System.currentTimeMillis() - t1;
                runtime = t1;
                System.out.println("Execution time: " + t1 + " ms");
            } catch (ConfigurationException e) {
                System.out.println("Configuration exception ");
            } catch (ClassNotFoundException e) {
                Logger.getLogger(Experiment.class.getName()).log(
                        Level.SEVERE, null, e);
            } catch (IllegalAccessException e) {
                Logger.getLogger(Experiment.class.getName()).log(
                        Level.SEVERE, null, e);
            } catch (InstantiationException e) {
                Logger.getLogger(Experiment.class.getName()).log(
                        Level.SEVERE, null, e);
            }
        } else {
            System.out.println("Job file not found");
            System.exit(1);
        }
    }

    /**
     * The evaluation method used
     * 
     * @return The evaluation method used.
     */
    public IEvaluationMethod getMethod() {
        return method;
    }

    /**
     * Time of execution of the program in miliseconds
     * 
     * @return Get the run time
     */
    public long getRuntime() {
        return runtime;
    }
}
