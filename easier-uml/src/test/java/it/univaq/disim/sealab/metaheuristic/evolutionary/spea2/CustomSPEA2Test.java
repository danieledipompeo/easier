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
import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CustomSPEA2Test<S extends UMLRSolution> extends CustomGeneticAlgorithmTest<S> {



    @BeforeEach
    public void setUp() {
        super.setUp();

        SPEA2Builder<S> customBuilder = new CustomSPEA2Builder<>(p, crossoverOperator,
                mutationOperator).setMaxIterations(4).setPopulationSize(2)
                .setSolutionListEvaluator(solutionListEvaluator);

        algorithm = customBuilder.build();
    }


    @Test
    public void populationToCsVTest() throws IOException {
        UMLRSolution sol = p.createSolution();
//        sol.setPerfQ(-10);
//        sol.setReliability(-10);
//        sol.setPAs(0);
//		sol.getVariable(0).setNumOfChanges(10);
        for(int objectiveIndex = 0; objectiveIndex <= sol.getObjectives().length; objectiveIndex++)
            sol.setObjective(objectiveIndex, new Random().nextDouble());

        ((CustomSPEA2<UMLRSolution>) algorithm).setPopulation(List.of(sol));

        ((CustomSPEA2<UMLRSolution>) algorithm).populationToCSV();

        LineNumberReader lnr = new LineNumberReader(new FileReader(Configurator.eINSTANCE.getOutputFolder().resolve("solution_dump.csv").toString()));
        lnr.lines().count();
        assertTrue(lnr.getLineNumber() == 2);
        Files.delete(Configurator.eINSTANCE.getOutputFolder().resolve("solution_dump.csv"));
    }


    @Test
    public void runTest() throws IOException {
        super.runTest();
    }

}
