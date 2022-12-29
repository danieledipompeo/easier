package it.univaq.disim.sealab.metaheuristic.evolutionary.nsgaii;

import it.univaq.disim.sealab.metaheuristic.evolutionary.CustomGeneticAlgorithmTest;
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

import static org.junit.jupiter.api.Assertions.*;

public class CustomNSGAIITest<S extends UMLRSolution> extends CustomGeneticAlgorithmTest<S> {

    @BeforeEach
    public void setUp() {
        super.setUp();
        NSGAIIBuilder<S> customNSGABuilder = new CustomNSGAIIBuilder<S>(p, crossoverOperator,
                mutationOperator, Configurator.eINSTANCE.getPopulationSize()).setMaxEvaluations(4)
                .setSolutionListEvaluator(solutionListEvaluator);

        algorithm = customNSGABuilder.build();
    }

    @Test
    public void isLocalOptimalPointSolutionWithListOfSolution() {
        super.isLocalOptimalPointSolutionWithListOfSolution();

        ((CustomNSGAII<S>) algorithm).oldPopulation = solutions;

        assertFalse(((CustomNSGAII<S>) algorithm).isStagnantState());
    }

    @Test
    public void isLocalOptimalPointSolutionWithListOfSolutionShouldReturnFalse() {
        super.isLocalOptimalPointSolutionWithListOfSolutionShouldReturnFalse();
        ((CustomNSGAII<S>) algorithm).oldPopulation = solutions;

        assertFalse(((CustomNSGAII<S>) algorithm).isStagnantState());
    }

    @Test
    public void runTest() throws IOException {
        super.runTest();
    }
}
