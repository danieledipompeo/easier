package it.univaq.disim.sealab.metaheuristic.evolutionary.pesaii;

import it.univaq.disim.sealab.metaheuristic.evolutionary.CustomAlgorithmTest;
import it.univaq.disim.sealab.metaheuristic.evolutionary.CustomGeneticAlgorithmTest;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.uma.jmetal.algorithm.multiobjective.pesa2.PESA2Builder;

import java.io.IOException;
import java.nio.file.Files;

public class CustomPESA2Test<S extends UMLRSolution> extends CustomGeneticAlgorithmTest<S> {

    @BeforeEach
    public void setUp() {
        super.setUp();

        PESA2Builder<S> customBuilder = new CustomPESA2Builder<>(p, crossoverOperator,
                mutationOperator).setMaxEvaluations(4).setPopulationSize(2)
                .setSolutionListEvaluator(solutionListEvaluator);

        algorithm = customBuilder.build();
    }

    @Test
    public void isLocalOptimalPointSolutionWithListOfSolution() {
        super.isLocalOptimalPointSolutionWithListOfSolution();
    }

    @Test
    public void isLocalOptimalPointSolutionWithListOfSolutionShouldReturnFalse() {
        super.isLocalOptimalPointSolutionWithListOfSolutionShouldReturnFalse();
    }


    @Test
    public void populationToCsVTest() throws IOException {
//		UMLRSolution sol = p.createSolution();
//		sol.setPerfQ(-10);
//		sol.setReliability(-10);
//		sol.setPAs(0);
//		sol.getVariable(0).setNumOfChanges(10);
//		algorithm.setPopulation(List.of(sol));
//
//		((CustomPESA2<UMLRSolution>) algorithm).populationToCSV();
//
//		LineNumberReader lnr = new LineNumberReader(new FileReader(Configurator.eINSTANCE.getOutputFolder().resolve("solution_dump.csv").toString()));
//		lnr.lines().count();
//		assertTrue(lnr.getLineNumber() == 2);
//		Files.delete(Configurator.eINSTANCE.getOutputFolder().resolve("solution_dump.csv"));
    }

    @Test
    public void runTest() throws IOException {
        super.runTest();
    }

}
