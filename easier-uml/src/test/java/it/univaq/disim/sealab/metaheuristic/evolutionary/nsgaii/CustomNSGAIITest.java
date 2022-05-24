package it.univaq.disim.sealab.metaheuristic.evolutionary.nsgaii;

import it.univaq.disim.sealab.metaheuristic.evolutionary.CustomAlgorithmTest;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class CustomNSGAIITest<S extends RSolution<?>> extends CustomAlgorithmTest<S> {

//    CustomNSGAII<UMLRSolution> algorithm;

    @BeforeEach
    public void setUp() {
        super.setUp();
        NSGAIIBuilder<UMLRSolution> customNSGABuilder = new CustomNSGAIIBuilder<UMLRSolution>(p, crossoverOperator,
                mutationOperator, Configurator.eINSTANCE.getPopulationSize()).setMaxEvaluations(4)
                .setSolutionListEvaluator(solutionListEvaluator);

        algorithm = customNSGABuilder.build();
    }

    @Test
    public void isLocalOptimalPointSolutionWithListOfSolution() {
        super.isLocalOptimalPointSolutionWithListOfSolution();

        ((CustomNSGAII) algorithm).oldPopulation = solutions;

        assertFalse(((CustomNSGAII) algorithm).isStagnantState());
    }

    @Test
    public void isLocalOptimalPointSolutionWithListOfSolutionShouldReturnFalse() {
        super.isLocalOptimalPointSolutionWithListOfSolutionShouldReturnFalse();
        ((CustomNSGAII) algorithm).oldPopulation = solutions;

        assertFalse(((CustomNSGAII) algorithm).isStagnantState());
    }

    @Test
    public void updateProgressTest() throws IOException {
        super.updateProgressTest();

        ((CustomNSGAII) algorithm).updateProgress();
        Path output = Configurator.eINSTANCE.getOutputFolder().resolve("algo_perf_stats.csv");
        assertTrue("The algo_perf_stats.csv should exist", Files.exists(output));

        String header = "algorithm,problem_tag,execution_time(ms),total_memory_before(B),free_memory_before(B),total_memory_after(B),free_memory_after(B)";
        try (BufferedReader br = new BufferedReader(new FileReader(output.toFile()))) {
            String line = br.readLine();
            assertEquals(header, line); //The first must be the header
        }

        output = Configurator.eINSTANCE.getOutputFolder().resolve("solution_dump.csv");
        assertTrue("The solution_dump.csv file should exist", Files.exists(output));
        header = "algorithm,problem_tag,solID,perfQ,#changes,pas,reliability";
        try (BufferedReader br = new BufferedReader(new FileReader(output.toFile()))) {
            String line = br.readLine();
            assertEquals(header, line); // The first must be the header
        }

        LineNumberReader lnr = new LineNumberReader(
                new FileReader(output.toFile()));

        lnr.lines().count();
        assertTrue(lnr.getLineNumber() == 2);
    }


//    @Test
//    public void runTest() throws IOException {
//        algorithm.run();
//
//        Path output = Configurator.eINSTANCE.getOutputFolder().resolve("algo_perf_stats.csv");
//        assertTrue("The algo_perf_stats.csv should exist", Files.exists(output));
//
//        String header = "algorithm,problem_tag,execution_time(ms),total_memory_before(B),free_memory_before(B),total_memory_after(B),free_memory_after(B)";
//        try (BufferedReader br = new BufferedReader(new FileReader(output.toFile()))) {
//            String line = br.readLine();
//            System.out.println(line);
//            assertEquals(header, line); //The first must be the header
//        }
//    }
}
