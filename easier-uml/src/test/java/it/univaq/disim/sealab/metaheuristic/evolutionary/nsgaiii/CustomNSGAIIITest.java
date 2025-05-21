package it.univaq.disim.sealab.metaheuristic.evolutionary.nsgaiii;

import it.univaq.disim.sealab.metaheuristic.evolutionary.CustomGeneticAlgorithmTest;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import it.univaq.disim.sealab.metaheuristic.utils.EasierException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;


public class CustomNSGAIIITest<S extends UMLRSolution> extends CustomGeneticAlgorithmTest<S> {

    @BeforeEach
    public void setUp() {
        super.setUp();
        NSGAIIIBuilder<S> customNSGAIIIBuilder = new CustomNSGAIIIBuilder<>(p, crossoverOperator, mutationOperator, Configurator.eINSTANCE.getPopulationSize())
                .setMaxIterations(4)
                .setSolutionListEvaluator(solutionListEvaluator)
                .setNumberOfDivisions(Configurator.eINSTANCE.getNumberOfDivisions());

        // Create mock of Configurator
//    Configurator mockConfigurator = mock(Configurator.class);
    Configurator spiedConfigurator = spy(Configurator.eINSTANCE);

    // Define behavior for getObjectiveList
//    when(mockConfigurator.getObjectivesList()).thenReturn(
//        Arrays.asList("sysRespT", "changes", "reliability", "energy")
//    );
    doReturn(Arrays.asList("sysRespT", "changes", "reliability", "energy")).when(spiedConfigurator).getObjectivesList();

    // Mock other necessary methods that are used in the test
//    when(mockConfigurator.getPopulationSize()).thenReturn(2);
//    when(mockConfigurator.getNumberOfDivisions()).thenReturn(2);
    doReturn(Paths.get("/tmp/easier-output-test")).when(spiedConfigurator).getOutputFolder();

    // Set the mock instance
//    Configurator.eINSTANCE = mockConfigurator;


        algorithm = customNSGAIIIBuilder.build();
    }

    @Test
    public void testUpdateProgress() throws IOException, EasierException {
        super.updateProgressTest();

        ((CustomNSGAIII<S>) algorithm).updateProgress();
        
        // Verify algo_perf_stats.csv
        Path output = Configurator.eINSTANCE.getOutputFolder().resolve("algo_perf_stats.csv");
        assertTrue(Files.exists(output),"The algo_perf_stats.csv should exist");

        String expectedHeader = "algorithm,problem_tag,execution_time(ms),total_memory_before(B),free_memory_before(B),total_memory_after(B),free_memory_after(B)";
        try (BufferedReader br = new BufferedReader(new FileReader(output.toFile()))) {
            String actualHeader = br.readLine();
            assertEquals(expectedHeader, actualHeader, "Header should match expected format");
        }

        // Verify solution_dump.csv
        output = Configurator.eINSTANCE.getOutputFolder().resolve("solution_dump.csv");
        assertTrue(Files.exists(output),"The solution_dump.csv file should exist");
        
        expectedHeader = "algorithm,problem_tag,solID,perfQ,#changes,pas,reliability";
        try (BufferedReader br = new BufferedReader(new FileReader(output.toFile()))) {
            String actualHeader = br.readLine();
            assertEquals(expectedHeader, actualHeader, "Header should match expected format");
        }

        // Verify number of lines in solution dump
        try (LineNumberReader lnr = new LineNumberReader(new FileReader(output.toFile()))) {
            lnr.skip(Long.MAX_VALUE);
            assertEquals(2, lnr.getLineNumber(), "Should have header + one solution line");
        }
    }

    @Test
    public void testRun() throws IOException {
        super.runTest();
    }
    
    @Test
    public void testIsStoppingConditionReached() {
        CustomNSGAIII<S> customNsgaiii = (CustomNSGAIII<S>) algorithm;
        
        // Test default condition
        assertFalse(customNsgaiii.isStoppingConditionReached(),
            "Should not reach stopping condition initially");

        // Force evaluations to exceed max
        for (int i = 0; i < 5; i++) {
            customNsgaiii.updateProgress();
        }
        
        assertTrue(customNsgaiii.isStoppingConditionReached(),
            "Should reach stopping condition after max evaluations");
    }

    @Test
    public void testPopulationToCSV() throws IOException {
        CustomNSGAIII<S> customNsgaiii = (CustomNSGAIII<S>) algorithm;
        
        // Add test solution to population
        S solution = p.createSolution();
        customNsgaiii.getPopulation().add(solution);
        
        // Test CSV output
        customNsgaiii.populationToCSV();
        
        Path output = Configurator.eINSTANCE.getOutputFolder().resolve("solution_dump.csv");
        assertTrue(Files.exists(output),"CSV file should be created");
        
        try (BufferedReader br = new BufferedReader(new FileReader(output.toFile()))) {
            String header = br.readLine();
            assertNotNull(header,"Should have header line");
            String dataLine = br.readLine(); 
            assertNotNull(dataLine,"Should have data line");
            assertTrue(dataLine.startsWith(customNsgaiii.getName()),"Data line should contain algorithm name");
        }
    }
}