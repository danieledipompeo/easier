package it.univaq.disim.sealab.metaheuristic.evolutionary.experiment;

import it.univaq.disim.sealab.metaheuristic.domain.EasierExperimentDAO;
import it.univaq.disim.sealab.metaheuristic.domain.EasierParetoDAO;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.FileUtils;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class RExecuteAlgorithms<S extends RSolution<?>, Result extends List<S>> {

    protected RExperiment<S, Result> experiment;

    /**
     * Constructor
     */
    public RExecuteAlgorithms(RExperiment<S, Result> exp) {
        this.experiment = exp;
    }

    public RExecuteAlgorithms<S, Result> run() {
        JMetalLogger.logger.info("ExecuteAlgorithms: Preparing output directory");
        prepareOutputDirectory();

        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",
                "" + this.experiment.getNumberOfCores());

        for (ExperimentAlgorithm<S, Result> algo : experiment.getAlgorithmList()) {
            algo.runAlgorithm(this.experiment);
            List<RSolution<?>> population = (List<RSolution<?>>) algo.getAlgorithm().getResult();

            EasierExperimentDAO.eINSTANCE.addSuperPareto(new EasierParetoDAO(population,
                    Configurator.eINSTANCE.getMaxEvaluation().get(0), algo.getRunId()));
        }

        FileUtils.moveTmpFile(Configurator.eINSTANCE.getTmpFolder(),
                Paths.get(Configurator.eINSTANCE.getOutputFolder().toString(), "tmp"));
        return this;
    }

    protected void prepareOutputDirectory() {
        Path expBaseDir = Paths.get(experiment.getExperimentBaseDirectory());
        if (!Files.exists(expBaseDir)) {
            createExperimentDirectory();
        }
    }

    /**
     * First empty tmp and experiment base DIRs,
     * then remove directories
     * finally create both directories
     */
    private void createExperimentDirectory() {

        if (Files.exists(Paths.get(experiment.getExperimentBaseDirectory()))) {
            try (Stream<Path> walker = Files.walk(Paths.get(experiment.getExperimentBaseDirectory()))) {
                walker.sorted(Comparator.reverseOrder())
                        .map(Path::toFile).forEach(File::delete);

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        if (Files.exists(Configurator.eINSTANCE.getTmpFolder())) {
            try (Stream<Path> walker = Files.walk(Configurator.eINSTANCE.getTmpFolder())) {
                walker.sorted(Comparator.reverseOrder()).map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        try {
            Files.createDirectories(Paths.get(experiment.getExperimentBaseDirectory()));
            Files.createDirectories(Configurator.eINSTANCE.getTmpFolder());
        } catch (IOException e) {
            throw new JMetalException(String.format("Error creating experiment and temp directories: %s \t %s",
                    experiment.getExperimentBaseDirectory(), Configurator.eINSTANCE.getTmpFolder()));
        }
    }

}
