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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.sf.jclal.util.file.FileUtil;
import weka.gui.ExtensionFileFilter;

/**
 * Experiments runner.
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 */
public class RunExperiment {

    /**
     * Number of processors to use.
     */
    private static int numberOfProcessors = 1;
    /**
     * List of configuration files.
     */
    private static String cfgs[];
    /**
     * Usage message error
     */
    private static final String USAGE_MES = "\tnet.sf.jclal.RunExperiment <experiment file>\t\t(Execute experiment)";

    /**
     * JCLAL main method
     *
     * @param args These are the possible arguments:
     * <p>
     * [optional] -processors=[int] or [all] to use all the possible processors.
     * Default value is 1.
     * </p>
     * <p>
     * -cfg="[list of configuration files]"
     * </p>
     * <p>
     * -d=\"[A directory that constains a set of experiment files. This option
     * can be used to execute a battery of experiments]"
     * </p>
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage:" + USAGE_MES);
            System.out
                    .println("These are the possible arguments:"
                            + "\n [optional] -processors=[int] or [all] to use all the possible processors. The default value is 1"
                            + "\n -cfg=\"[list of configuration files]\": List of the configuration files. The name of each configuration file must be followed by a semicolon (;), except the last"
                            + "\n -d=\"[A directory that constains a set of experiment files. This option can be used to execute a battery of experiments]\"");
            System.exit(1);
        } else {

            ExperimentBuilder builder = new ExperimentBuilder();

            extractArguments(args);

            System.out.println("Initializing job...");

            ExecutorService threadExecutor = Executors
                    .newFixedThreadPool(numberOfProcessors);

            // Expand the processes and execute them
            for (String cfg : cfgs) {

                threadExecutor.execute(new ExperimentThread(new Experiment(),
                        builder.buildExperiment(cfg)));

            }

            threadExecutor.shutdown();

            try {
                if (!threadExecutor.awaitTermination(30, TimeUnit.DAYS)) {
                    System.out.println("Threadpool timeout occurred");
                }
            } catch (InterruptedException ie) {
                System.err
                        .println("Threadpool prematurely terminated due to interruption in thread that created pool");
            }

        }
    }

    /**
     * <p>
     * Utility method to extract the arguments of the configuration, number
     * of processors and configuration files</p>
     *
     * @param args the main arguments
     */
    public static void extractArguments(String[] args) {

        for (String arg : args) {

            if (arg.startsWith("-processors")) {
                StringTokenizer tokens = new StringTokenizer(arg, "=");
                tokens.nextToken();
                String pro = tokens.nextToken();

                if (pro.equalsIgnoreCase("all")) {
                    numberOfProcessors = Runtime.getRuntime().availableProcessors();
                } else {
                    numberOfProcessors = Integer.parseInt(pro);
                }

                continue;
            }

            if (arg.startsWith("-cfg")) {

                StringTokenizer tokens = new StringTokenizer(arg, "=");
                tokens.nextToken();
                tokens = new StringTokenizer(tokens.nextToken(), ";");

                int index = 0;

                cfgs = new String[tokens.countTokens()];

                while (tokens.hasMoreTokens()) {

                    cfgs[index++] = tokens.nextToken();

                }

                continue;

            }

            if (arg.startsWith("-d")) {

                StringTokenizer tokens = new StringTokenizer(arg, "=");
                tokens.nextToken();
                String directory = tokens.nextToken();

                File a = new File(directory);

                if (a.exists() && a.isDirectory()) {

                    FilenameFilter filter = new ExtensionFileFilter(".cfg", "The experiment configuration files");

                    List<File> files = new ArrayList<File>();
                    FileUtil.getFilesInDirectory(a, filter, files);
                    FileUtil.orderFilesByPathName(files, true);

                    int index = 0;

                    cfgs = new String[files.size()];

                    for (File file : files) {
                        cfgs[index++] = file.getPath();
                    }
                } else {
                    System.err.println("The directory does not exist");
                    System.exit(1);
                }
            }

        }

    }
}
