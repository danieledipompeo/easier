package it.univaq.disim.sealab.metaheuristic.evolutionary.random;

import it.univaq.disim.sealab.metaheuristic.evolutionary.CustomAlgorithmTest;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.uma.jmetal.algorithm.multiobjective.randomsearch.RandomSearchBuilder;

import java.io.IOException;

public class CustomRandomSearchTest<S extends UMLRSolution> extends CustomAlgorithmTest<S> {

//    CustomNSGAII<UMLRSolution> algorithm;

    @BeforeEach
    public void setUp() {
        super.setUp();
        int iteration = 4;
        RandomSearchBuilder<S> customBuilder = new CustomRandomSearchBuilder<>(p);
        customBuilder.setMaxEvaluations(iteration);

        algorithm = customBuilder.build();
    }

    @Test
    public void runTest() throws IOException {
        super.runTest();

    }
}
