package it.univaq.disim.sealab.metaheuristic.evolutionary.operator;

import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UMLRCrossoverTest {

    UMLRCrossover<UMLRSolution> xOver;

    // we must guarantee that the xOver operation will be executed
    double xOverProb = 1;
    Path modelPath;

    @BeforeEach
    public void setUp() throws Exception {
        xOver = new UMLRCrossover<>(xOverProb);
        modelPath = Paths.get(getClass().getResource("/models/simplified-cocome/cocome.uml").getPath());
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @RepeatedTest(1)
    public void execute() {

        Path modelPath = Paths.get(getClass().getResource("/simplified-cocome/cocome.uml").getPath());

        UMLRSolution parent1 = new UMLRSolution(modelPath, "simplied-cocome__test");
        parent1.createRandomRefactoring();
        UMLRSolution parent2 = new UMLRSolution(modelPath, "simplied-cocome__test");
        parent2.createRandomRefactoring();

        List<UMLRSolution> population = xOver.execute(List.of(parent1, parent2));
        assertNotNull(population, "Population after xOver should not be null");

        if (population.get(0).isCrossover())
            assertNotEquals(parent1, population.get(0), String.format("%s \t %s", parent1, population.get(0)));
    }

    @Test
    public void crossover_is_not_allowed_with_same_parents() {

        Path modelPath = Paths.get(getClass().getResource("/simplified-cocome/cocome.uml").getPath());

        UMLRSolution parent1 = new UMLRSolution(modelPath, "simplied-cocome__test");
        parent1.createRandomRefactoring();

        List<UMLRSolution> population = xOver.execute(List.of(parent1, parent1));
        assertNotNull(population, "The crossover must always return a not null population.");

        population.stream().forEach(p -> assertFalse(p.isCrossover(), "Expected unfeasible crossover with two " +
                "identical parents."));
    }

    @Test
    void independentSequence() {
        UMLRSolution sol = new UMLRSolution(modelPath, "simplied-cocome__test");
        sol.createRandomRefactoring();
        xOver.independentSequence(sol);
    }

}