package it.univaq.disim.sealab.metaheuristic.evolutionary;

import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.UMLRCrossover;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.UMLRMutation;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.UMLRSolutionListEvaluator;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

public class CustomAlgorithmTest<S> {

    protected final CrossoverOperator<UMLRSolution> crossoverOperator = new UMLRCrossover(
            Configurator.eINSTANCE.getXoverProbabiliy());
    protected final MutationOperator<UMLRSolution> mutationOperator = new UMLRMutation(
            Configurator.eINSTANCE.getMutationProbability(), Configurator.eINSTANCE.getDistributionIndex());
    protected final SelectionOperator<List<UMLRSolution>, UMLRSolution> selectionOpertor = new BinaryTournamentSelection<UMLRSolution>(
            new RankingAndCrowdingDistanceComparator<UMLRSolution>());
    protected final SolutionListEvaluator<UMLRSolution> solutionListEvaluator = new UMLRSolutionListEvaluator<>();
    protected UMLRProblem<UMLRSolution> p;
    List<ExperimentAlgorithm<UMLRSolution, List<UMLRSolution>>> algorithms = new ArrayList<>();

    protected List<UMLRSolution> solutions;

    protected AbstractGeneticAlgorithm algorithm;

    @BeforeAll
    public static void setUpClass() throws IOException {
        Files.createDirectories(Configurator.eINSTANCE.getOutputFolder());
        Files.createDirectories(Configurator.eINSTANCE.getTmpFolder());
    }

//    @AfterAll
    public static void tearDownClass() throws IOException {
        Files.walk(Configurator.eINSTANCE.getOutputFolder())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);

    }

    public void setUp() {
        String modelpath = getClass().getResource("/models/train-ticket/train-ticket.uml").getFile();
        p = new UMLRProblem<>(Paths.get(modelpath), "problem_for_testing");
    }

    public void isLocalOptimalPointSolutionWithListOfSolution() {
        solutions = new ArrayList<>();
        int i = 0;
        while (i < 2) {
            UMLRSolution sol = p.createSolution();
            sol.setPerfQ(-10);
            sol.setReliability(-10);
            sol.setPAs(0);
//			sol.getVariable(0).setNumOfChanges(10);
//			sol.getVariable(0).setNumOfChanges(10);
            solutions.add(sol);
            i++;
        }

        algorithm.setPopulation(solutions);

    }

    public void isLocalOptimalPointSolutionWithListOfSolutionShouldReturnFalse() {
        solutions = new ArrayList<>();
        int i = 0;
        while (i < 2) {
            UMLRSolution sol = p.createSolution();
            sol.setPerfQ(-10);
            sol.setReliability(-10);
            sol.setPAs(0);
//			sol.getVariable(0).setNumOfChanges(10);
//			sol.getVariable(0).setNumOfChanges(10);
            solutions.add(sol);
            i++;
        }
        algorithm.setPopulation(solutions);

        solutions = new ArrayList<>();

        i = 0;
        while (i < 2) {
            UMLRSolution sol = p.createSolution();
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

    }

    public void updateProgressTest() throws IOException {
        UMLRSolution sol = p.createSolution();
        sol.setPerfQ(-10);
        sol.setReliability(-10);
        sol.setPAs(0);
        solutions.add(sol);

//		sol.getVariable(0).setNumOfChanges(10);
        algorithm.setPopulation(solutions);

    }


    public void runTest() throws IOException {
        algorithm.run();

        Path output = Configurator.eINSTANCE.getOutputFolder().resolve("algo_perf_stats.csv");
        assertTrue("The algo_perf_stats.csv should exist", Files.exists(output));

        String header = "algorithm,problem_tag,execution_time(ms),total_memory_before(B),free_memory_before(B),total_memory_after(B),free_memory_after(B)";
        try (BufferedReader br = new BufferedReader(new FileReader(output.toFile()))) {
            String line = br.readLine();
            System.out.println(line);
            assertEquals(header, line); //The first must be the header
        }
    }
}
