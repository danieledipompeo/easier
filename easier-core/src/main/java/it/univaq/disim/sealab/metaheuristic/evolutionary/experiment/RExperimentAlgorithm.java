package it.univaq.disim.sealab.metaheuristic.evolutionary.experiment;

import it.univaq.disim.sealab.metaheuristic.evolutionary.RProblem;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.experiment.util.RSolutionListOutput;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.lab.experiment.Experiment;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.lab.experiment.util.ExperimentProblem;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import java.io.File;
import java.util.List;

public class RExperimentAlgorithm<S extends RSolution<?>, Result extends List<S>>
        extends ExperimentAlgorithm<S, Result> {

    private RProblem<S> pr;

    /**
     * Constructor
     */
    public RExperimentAlgorithm(Algorithm<Result> algorithm, String algorithmTag, ExperimentProblem<S> problem,
                                int runId) {
        super(algorithm, algorithmTag, problem, runId);
        pr = (RProblem<S>) problem.getProblem();
    }

    public RExperimentAlgorithm(Algorithm<Result> algorithm, ExperimentProblem<S> problem, int runId) {
        this(algorithm, algorithm.getName(), problem, runId);
    }

    @Override
    public void runAlgorithm(Experiment<?, ?> experimentData) {
        String outputDirectoryName = experimentData.getExperimentBaseDirectory() + "/data/" + super.getAlgorithmTag()
                + "/" + super.getProblemTag();

        File outputDirectory = new File(outputDirectoryName);
        if (!outputDirectory.exists()) {
            boolean result = new File(outputDirectoryName).mkdirs();
            if (result) {
                JMetalLogger.logger.info("Creating " + outputDirectoryName);
            } else {
                JMetalLogger.logger.severe("Creating " + outputDirectoryName + " failed");
            }
        }

        String funFile = outputDirectoryName + "/" + experimentData.getOutputParetoFrontFileName() + super.getRunId();
        String varFile = outputDirectoryName + "/" + experimentData.getOutputParetoSetFileName() + super.getRunId();

        JMetalLogger.logger.info(" Running algorithm: " + super.getAlgorithmTag() + ", problem: "
                + super.getProblemTag() + ", run: " + super.getRunId() + ", funFile: " + funFile);

        try {
            super.getAlgorithm().run();

            Result population = super.getAlgorithm().getResult();

            String suffix = "__" + population.get(0).getProblemName() + ".csv";

            funFile += suffix;
            varFile += suffix;

            new RSolutionListOutput(population).setVarFileOutputContext(new DefaultFileOutputContext(varFile, ","))
                    .setFunFileOutputContext(new DefaultFileOutputContext(funFile, ",")).print();
        } catch (Exception exception) {
            JMetalLogger.logger.warning("Execution failed: " + funFile + " has not been created.");
            exception.printStackTrace();
        }
    }

}
