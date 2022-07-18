package it.univaq.disim.sealab.metaheuristic.evolutionary;

import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierResourcesLogger;
import org.junit.jupiter.api.BeforeAll;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CustomAlgorithmTest<S extends UMLRSolution> {


    protected UMLRProblem<S> p;
    protected List<S> solutions;
    protected Algorithm algorithm;

    List<ExperimentAlgorithm<S, List<S>>> algorithms = new ArrayList<>();

    @BeforeAll
    public static void setUpClass() throws IOException {
        Files.createDirectories(Configurator.eINSTANCE.getOutputFolder());
        Files.createDirectories(Configurator.eINSTANCE.getTmpFolder());
    }

    //    @AfterAll
    public static void tearDownClass() throws IOException {
        Files.walk(Configurator.eINSTANCE.getOutputFolder())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);

    }

    public void setUp() {
        String modelpath = getClass().getResource("/models/train-ticket/train-ticket.uml").getFile();
        p = new UMLRProblem<>(Paths.get(modelpath), "problem_for_testing");
    }


    public void runTest() throws IOException {
        algorithm.run();
        EasierResourcesLogger.dumpToCSV();

        Path output = Configurator.eINSTANCE.getOutputFolder().resolve("algo_perf_stats.csv");
        assertTrue("The algo_perf_stats.csv should exist", Files.exists(output));

        String header = "iteration_id,label,step,execution_time(ms),total_memory_before(B),free_memory_before(B),total_memory_after(B),free_memory_after(B)";
        try (BufferedReader br = new BufferedReader(new FileReader(output.toFile()))) {
            String line = br.readLine();
            assertEquals(header, line); //The first must be the header
        }
    }
}
