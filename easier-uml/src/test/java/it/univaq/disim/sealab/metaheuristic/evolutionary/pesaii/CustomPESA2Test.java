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
                mutationOperator).setMaxEvaluations(8).setPopulationSize(2)
                .setSolutionListEvaluator(solutionListEvaluator);

        algorithm = customBuilder.build();
    }

    @Test
    public void runTest() throws IOException {
        super.runTest();
    }

}
