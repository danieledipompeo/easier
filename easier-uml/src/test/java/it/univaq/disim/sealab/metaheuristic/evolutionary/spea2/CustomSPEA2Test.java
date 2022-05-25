package it.univaq.disim.sealab.metaheuristic.evolutionary.spea2;

import it.univaq.disim.sealab.metaheuristic.evolutionary.CustomAlgorithmTest;
import it.univaq.disim.sealab.metaheuristic.evolutionary.RSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRProblem;
import it.univaq.disim.sealab.metaheuristic.evolutionary.UMLRSolution;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.UMLRCrossover;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.UMLRMutation;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.UMLRSolutionListEvaluator;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CustomSPEA2Test<S extends RSolution<?>> extends CustomAlgorithmTest<S> {

//    SPEA2<UMLRSolution> algorithm;

//    @BeforeAll
//    public static void beforeClass() throws IOException {
//        Files.createDirectories(Configurator.eINSTANCE.getOutputFolder());
//    }

    @BeforeEach
    public void setUp() {
        super.setUp();

        SPEA2Builder<UMLRSolution> customBuilder = new CustomSPEA2Builder<UMLRSolution>(p, crossoverOperator,
                mutationOperator).setMaxIterations(72)
                .setSolutionListEvaluator(solutionListEvaluator);

        algorithm = customBuilder.build();
    }

    @Test
    public void isLocalOptimalPointSolutionWithListOfSolution() {
        List<UMLRSolution> solutions = new ArrayList<UMLRSolution>();
        int i = 0;
        while (i < 2) {
            UMLRSolution sol = (UMLRSolution) p.createSolution();
            sol.setPerfQ(-10);
            sol.setReliability(-10);
            sol.setPAs(0);
//			sol.getVariable(0).setNumOfChanges(10);
//			sol.getVariable(0).setNumOfChanges(10);
            solutions.add(sol);
            i++;
        }

        algorithm.setPopulation(solutions);
        ((CustomSPEA2<UMLRSolution>) algorithm).oldPopulation = solutions;

        assertFalse(((CustomSPEA2<UMLRSolution>) algorithm).isStagnantState());
    }

    @Test
    public void isLocalOptimalPointSolutionWithListOfSolutionShouldReturnFalse() {
        List<UMLRSolution> solutions = new ArrayList<UMLRSolution>();
        int i = 0;
        while (i < 2) {
            UMLRSolution sol = (UMLRSolution) p.createSolution();
            sol.setPerfQ(-10);
            sol.setReliability(-10);
            sol.setPAs(0);
//			sol.getVariable(0).setNumOfChanges(10);
//			sol.getVariable(0).setNumOfChanges(10);
            solutions.add(sol);
            i++;
        }
        algorithm.setPopulation(solutions);

        solutions = new ArrayList<UMLRSolution>();

        i = 0;
        while (i < 2) {
            UMLRSolution sol = (UMLRSolution) p.createSolution();
            sol.setPerfQ(-10);
            sol.setReliability(-10);
            sol.setPAs(0);
            if (i % 2 == 0)
                sol.setPAs(10);
//			sol.getVariable(0).setNumOfChanges(10);
//			sol.getVariable(0).setNumOfChanges(10);
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
//		sol.getVariable(0).setNumOfChanges(10);
        algorithm.setPopulation(List.of(sol));

        ((CustomSPEA2<UMLRSolution>) algorithm).populationToCSV();

        LineNumberReader lnr = new LineNumberReader(new FileReader(Configurator.eINSTANCE.getOutputFolder().resolve("solution_dump.csv").toString()));
        lnr.lines().count();
        assertTrue(lnr.getLineNumber() == 2);
        Files.delete(Configurator.eINSTANCE.getOutputFolder().resolve("solution_dump.csv"));
    }

}
