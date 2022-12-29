package it.univaq.disim.sealab.metaheuristic.evolutionary.spea2;

import it.univaq.disim.sealab.metaheuristic.evolutionary.CustomAlgorithmTest;
import it.univaq.disim.sealab.metaheuristic.evolutionary.CustomGeneticAlgorithmTest;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CustomSPEA2Test<S extends UMLRSolution> extends CustomGeneticAlgorithmTest<S> {

    @BeforeEach
    public void setUp() {
        super.setUp();

        SPEA2Builder<S> customBuilder = new CustomSPEA2Builder<>(p, crossoverOperator,
                mutationOperator).setMaxIterations(2).setPopulationSize(2)
                .setSolutionListEvaluator(solutionListEvaluator);

        algorithm = customBuilder.build();
    }

    @Test
    public void isLocalOptimalPointSolutionWithListOfSolution() {
        List<UMLRSolution> solutions = new ArrayList<>();
        int i = 0;
        while (i < 2) {
            UMLRSolution sol = p.createSolution();
            sol.setPerfQ(-10);
            sol.setReliability(-10);
            sol.setPAs(0);
            solutions.add(sol);
            i++;
        }

        ((CustomSPEA2<UMLRSolution>) algorithm).setPopulation(solutions);
        ((CustomSPEA2<UMLRSolution>) algorithm).oldPopulation = solutions;

        assertFalse(((CustomSPEA2<UMLRSolution>) algorithm).isStagnantState());
    }

    @Test
    public void isLocalOptimalPointSolutionWithListOfSolutionShouldReturnFalse() {
        List<UMLRSolution> solutions = new ArrayList<UMLRSolution>();
        int i = 0;
        while (i < 2) {
            UMLRSolution sol = p.createSolution();
            sol.setPerfQ(-10);
            sol.setReliability(-10);
            sol.setPAs(0);
            solutions.add(sol);
            i++;
        }
        ((CustomSPEA2<UMLRSolution>) algorithm).setPopulation(solutions);

        solutions = new ArrayList<>();

        i = 0;
        while (i < 2) {
            UMLRSolution sol = p.createSolution();
            sol.setPerfQ(-10);
            sol.setReliability(-10);
            sol.setPAs(0);
            if (i % 2 == 0)
                sol.setPAs(10);
            solutions.add(sol);
            i++;
        }

        ((CustomSPEA2<UMLRSolution>) algorithm).oldPopulation = solutions;

        assertFalse(((CustomSPEA2<UMLRSolution>) algorithm).isStagnantState());
    }


    @Test
    public void populationToCsVTest() throws IOException {
        UMLRSolution sol = p.createSolution();
        sol.setPerfQ(-10);
        sol.setReliability(-10);
        sol.setPAs(0);
        ((CustomSPEA2<UMLRSolution>) algorithm).setPopulation(List.of(sol));

        ((CustomSPEA2<UMLRSolution>) algorithm).populationToCSV();

        LineNumberReader lnr = new LineNumberReader(new FileReader(Configurator.eINSTANCE.getOutputFolder().resolve("solution_dump.csv").toString()));
        lnr.lines().count();
        assertEquals(2, lnr.getLineNumber());
        Files.delete(Configurator.eINSTANCE.getOutputFolder().resolve("solution_dump.csv"));
    }


    @Test
    public void runTest() throws IOException {
        super.runTest();
    }

}
