package it.univaq.disim.sealab.metaheuristic.evolutionary;

import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.UMLRCrossover;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.UMLRMutation;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.UMLRSolutionListEvaluator;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomGeneticAlgorithmTest<S extends UMLRSolution> extends CustomAlgorithmTest<S> {

    protected final CrossoverOperator<S> crossoverOperator = new UMLRCrossover<>(
            Configurator.eINSTANCE.getXoverProbabiliy());
    protected final MutationOperator<S> mutationOperator = new UMLRMutation<>(
            Configurator.eINSTANCE.getMutationProbability(), Configurator.eINSTANCE.getDistributionIndex());

    protected final SelectionOperator<List<S>, S> selectionOpertor = new BinaryTournamentSelection<>(
            new RankingAndCrowdingDistanceComparator<>());
    protected final SolutionListEvaluator<S> solutionListEvaluator = new UMLRSolutionListEvaluator<>();

    public void isLocalOptimalPointSolutionWithListOfSolution() {
        solutions = new ArrayList<>();
        int i = 0;
        while (i < 2) {
            S sol = p.createSolution();
            sol.setPerfQ(-10);
            sol.setReliability(-10);
            sol.setPAs(0);
//			sol.getVariable(0).setNumOfChanges(10);
//			sol.getVariable(0).setNumOfChanges(10);
            solutions.add(sol);
            i++;
        }

        ((AbstractGeneticAlgorithm<S, List<S>>) algorithm).setPopulation(solutions);

    }

    public void isLocalOptimalPointSolutionWithListOfSolutionShouldReturnFalse() {
        solutions = new ArrayList<>();
        int i = 0;
        while (i < 2) {
            S sol = p.createSolution();
            sol.setPerfQ(-10);
            sol.setReliability(-10);
            sol.setPAs(0);
//			sol.getVariable(0).setNumOfChanges(10);
//			sol.getVariable(0).setNumOfChanges(10);
            solutions.add(sol);
            i++;
        }
        ((AbstractGeneticAlgorithm<S, List<S>>) algorithm).setPopulation(solutions);

        solutions = new ArrayList<>();

        i = 0;
        while (i < 2) {
            S sol = p.createSolution();
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
        S sol = p.createSolution();
        sol.setPerfQ(-10);
        sol.setReliability(-10);
        sol.setPAs(0);
        solutions.add(sol);

//		sol.getVariable(0).setNumOfChanges(10);
        ((AbstractGeneticAlgorithm<S, List<S>>) algorithm).setPopulation(solutions);

    }


}
