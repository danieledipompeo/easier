package it.univaq.disim.sealab.metaheuristic.evolutionary.ibea;

import it.univaq.disim.sealab.metaheuristic.evolutionary.CustomGeneticAlgorithmTest;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class CustomIBEATest<S extends UMLRSolution> extends CustomGeneticAlgorithmTest<S> {

    @BeforeEach
    public void setUp() {
        super.setUp();
        CustomIBEABuilder<S> customBuilder = new CustomIBEABuilder<>(p, crossoverOperator,
                mutationOperator, Configurator.eINSTANCE.getPopulationSize()).setMaxEvaluations(4)
                .setSolutionListEvaluator(solutionListEvaluator);

        algorithm = customBuilder.build();
    }


    @AfterEach
    void tearDown() {
    }


    @Test
    public void runTest() throws IOException {
        super.runTest();
    }
}