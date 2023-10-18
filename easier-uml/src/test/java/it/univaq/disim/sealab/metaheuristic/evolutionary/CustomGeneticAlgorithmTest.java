package it.univaq.disim.sealab.metaheuristic.evolutionary;

import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.UMLRCrossover;
import it.univaq.disim.sealab.metaheuristic.evolutionary.operator.UMLRMutation;
import it.univaq.disim.sealab.metaheuristic.utils.Configurator;
import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomGeneticAlgorithmTest<S extends UMLRSolution> extends CustomAlgorithmTest<S> {

    protected final CrossoverOperator<S> crossoverOperator = new UMLRCrossover<>(
            Configurator.eINSTANCE.getXoverProbabiliy());
    protected final MutationOperator<S> mutationOperator = new UMLRMutation<>(
            Configurator.eINSTANCE.getMutationProbability(), Configurator.eINSTANCE.getDistributionIndex());

    protected final SelectionOperator<List<S>, S> selectionOpertor = new BinaryTournamentSelection<>(
            new RankingAndCrowdingDistanceComparator<>());
    protected final SolutionListEvaluator<S> solutionListEvaluator = new SequentialSolutionListEvaluator<>();

    public void updateProgressTest() throws IOException {
        S sol = p.createSolution();
//        sol.setPerfQ(-10);
//        sol.setReliability(-10);
//        sol.setPAs(0);

         for(int objectiveIndex = 0; objectiveIndex <= sol.getObjectives().length; objectiveIndex++)
            sol.setObjective(objectiveIndex, new Random().nextDouble());
        solutions.add(sol);

//		sol.getVariable(0).setNumOfChanges(10);
        ((AbstractGeneticAlgorithm<S, List<S>>) algorithm).setPopulation(solutions);

    }


}
